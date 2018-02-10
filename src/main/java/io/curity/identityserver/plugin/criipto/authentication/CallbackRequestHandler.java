/*
 *  Copyright 2017 Curity AB
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.curity.identityserver.plugin.criipto.authentication;

import io.curity.identityserver.plugin.criipto.config.CriiptoAuthenticatorPluginConfig;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.Nullable;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.attribute.Attributes;
import se.curity.identityserver.sdk.attribute.AuthenticationAttributes;
import se.curity.identityserver.sdk.attribute.ContextAttributes;
import se.curity.identityserver.sdk.attribute.SubjectAttributes;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.HttpRequest;
import se.curity.identityserver.sdk.http.HttpResponse;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.WebServiceClient;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static se.curity.identityserver.sdk.attribute.ContextAttributes.AUTH_TIME;

public class CallbackRequestHandler implements AuthenticatorRequestHandler<CallbackGetRequestModel>
{
    private final static Logger _logger = LoggerFactory.getLogger(CallbackRequestHandler.class);

    private final ExceptionFactory _exceptionFactory;
    private final CriiptoAuthenticatorPluginConfig _config;
    private final Json _json;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final WebServiceClientFactory _webServiceClientFactory;

    public CallbackRequestHandler(CriiptoAuthenticatorPluginConfig config)
    {
        _exceptionFactory = config.getExceptionFactory();
        _config = config;
        _json = config.getJson();
        _webServiceClientFactory = config.getWebServiceClientFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
    }

    @Override
    public CallbackGetRequestModel preProcess(Request request, Response response)
    {
        if (request.isGetRequest())
        {
            return new CallbackGetRequestModel(request);
        }
        else
        {
            throw _exceptionFactory.methodNotAllowed();
        }
    }

    @Override
    public Optional<AuthenticationResult> post(CallbackGetRequestModel requestModel, Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

    @Override
    public Optional<AuthenticationResult> get(CallbackGetRequestModel requestModel, Response response)
    {
        validateState(requestModel.getState());
        handleError(requestModel);

        Map<String, Object> tokenResponseData = redeemCodeForTokens(requestModel);

        try
        {
            //parse claims without need of key
            Map claimsMap = new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build()
                    .processToClaims(tokenResponseData.get("id_token").toString()).getClaimsMap();

            String[] userId = new String[1]; // Lambdas need data that is effectively final. String isn't; array is.

            _config.getCountry().getSweden().ifPresent(sweden ->
            {
                userId[0] = claimsMap.get("ssn").toString();
            });

            _config.getCountry().getNorway().ifPresent(norway ->
            {
                userId[0] = claimsMap.get("socialno").toString();
            });

            _config.getCountry().getDenmark().ifPresent(denmark ->
            {
                userId[0] = claimsMap.get("cprNumberIdentifier").toString();
            });

            Attributes subjectAttributes = Attributes.of(
                    Attribute.of("ssn", userId[0]),
                    Attribute.of("name", claimsMap.get("name").toString()));

            Attributes contextAttributes = Attributes.of(
                    Attribute.of("criipto_access_token", tokenResponseData.get("access_token").toString()),
                    Attribute.of("criipto_id_token", tokenResponseData.get("id_token").toString()));

            AuthenticationAttributes attributes = AuthenticationAttributes.of(
                    SubjectAttributes.of(userId[0], subjectAttributes),
                    ContextAttributes.of(contextAttributes));

            return Optional.of(new AuthenticationResult(attributes));
        }
        catch (Exception e)
        {
            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR,
                    "Invalid token " + e.getMessage());
        }
    }

    private Map<String, Object> redeemCodeForTokens(CallbackGetRequestModel requestModel)
    {
        HttpResponse tokenResponse = getWebServiceClient()
                .withPath("/oauth2/token")
                .request()
                .contentType("application/x-www-form-urlencoded")
                .body(getFormEncodedBodyFrom(createPostData(_config.getClientId(), _config.getClientSecret(),
                        requestModel.getCode(), requestModel.getRequestUrl())))
                .method("POST")
                .response();
        int statusCode = tokenResponse.statusCode();

        if (statusCode != 200)
        {
            if (_logger.isInfoEnabled())
            {
                _logger.info("Got error response from token endpoint: error = {}, {}", statusCode,
                        tokenResponse.body(HttpResponse.asString()));
            }

            throw _exceptionFactory.internalServerException(ErrorCode.EXTERNAL_SERVICE_ERROR);
        }

        return _json.fromJson(tokenResponse.body(HttpResponse.asString()));
    }

    private WebServiceClient getWebServiceClient()
    {
        Optional<HttpClient> httpClient = _config.getHttpClient();

        if (httpClient.isPresent())
        {
            return _webServiceClientFactory.create(httpClient.get()).withHost(_config.getDomain());
        }
        else
        {
            return _webServiceClientFactory.create(URI.create("https://" + _config.getDomain()));
        }
    }

    private void handleError(CallbackGetRequestModel requestModel)
    {
        if (!Objects.isNull(requestModel.getError()))
        {
            if ("access_denied".equals(requestModel.getError()))
            {
                _logger.debug("Got an error from Criipto: {} - {}", requestModel.getError(), requestModel
                        .getErrorDescription());

                throw _exceptionFactory.redirectException(
                        _authenticatorInformationProvider.getAuthenticationBaseUri().toASCIIString());
            }

            _logger.warn("Got an error from Criipto: {} - {}", requestModel.getError(), requestModel
                    .getErrorDescription());

            throw _exceptionFactory.externalServiceException("Login with Criipto failed");
        }
    }

    private static Map<String, String> createPostData(String clientId, String clientSecret, String code, String
            callbackUri)
    {
        Map<String, String> data = new HashMap<>(5);

        data.put("client_id", clientId);
        data.put("client_secret", clientSecret);
        data.put("code", code);
        data.put("grant_type", "authorization_code");
        data.put("redirect_uri", callbackUri);

        return data;
    }

    private static HttpRequest.BodyProcessor getFormEncodedBodyFrom(Map<String, String> data)
    {
        StringBuilder stringBuilder = new StringBuilder();

        data.entrySet().forEach(e -> appendParameter(stringBuilder, e));

        return HttpRequest.fromString(stringBuilder.toString());
    }

    private static void appendParameter(StringBuilder stringBuilder, Map.Entry<String, String> entry)
    {
        String key = entry.getKey();
        String value = entry.getValue();
        String encodedKey = urlEncodeString(key);
        stringBuilder.append(encodedKey);

        if (!Objects.isNull(value))
        {
            String encodedValue = urlEncodeString(value);
            stringBuilder.append("=").append(encodedValue);
        }

        stringBuilder.append("&");
    }

    private static String urlEncodeString(String unencodedString)
    {
        try
        {
            return URLEncoder.encode(unencodedString, StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("This server cannot support UTF-8!", e);
        }
    }

    private void validateState(String state)
    {
        @Nullable Attribute sessionAttribute = _config.getSessionManager().get("state");

        if (sessionAttribute != null && state.equals(sessionAttribute.getValueOfType(String.class)))
        {
            _logger.debug("State matches session");
        }
        else
        {
            _logger.debug("State did not match session");

            throw _exceptionFactory.badRequestException(ErrorCode.INVALID_SERVER_STATE, "Bad state provided");
        }
    }
}
