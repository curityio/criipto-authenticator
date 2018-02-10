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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.curity.identityserver.sdk.attribute.Attribute;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.errors.ErrorCode;
import se.curity.identityserver.sdk.http.RedirectStatusCode;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static io.curity.identityserver.plugin.criipto.descriptor.CriiptoAuthenticatorPluginDescriptor.CALLBACK;

public class CriiptoAuthenticatorRequestHandler implements AuthenticatorRequestHandler<Request>
{
    private static final Logger _logger = LoggerFactory.getLogger(CriiptoAuthenticatorRequestHandler.class);
    private final String AUTHORIZATION_ENDPOINT;

    private final CriiptoAuthenticatorPluginConfig _config;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;
    private final ExceptionFactory _exceptionFactory;

    public CriiptoAuthenticatorRequestHandler(CriiptoAuthenticatorPluginConfig config)
    {
        _config = config;
        _exceptionFactory = config.getExceptionFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
        AUTHORIZATION_ENDPOINT = "https://" + _config.getDomain() + "/oauth2/authorize";
    }

    @Override
    public Optional<AuthenticationResult> get(Request request, Response response)
    {
        _logger.debug("GET request received for authentication authentication");

        String redirectUri = createRedirectUri();
        String state = UUID.randomUUID().toString();
        Map<String, Collection<String>> queryStringArguments = new LinkedHashMap<>(6);
        Set<String> scopes = new LinkedHashSet<>(2);
        Set<String> acrValues = new LinkedHashSet<>(1);

        scopes.add("openid");
        setAcrValues(acrValues);

        _config.getSessionManager().put(Attribute.of("state", state));

        queryStringArguments.put("client_id", Collections.singleton(_config.getClientId()));
        queryStringArguments.put("redirect_uri", Collections.singleton(redirectUri));
        queryStringArguments.put("state", Collections.singleton(state));
        queryStringArguments.put("response_type", Collections.singleton("code"));

        queryStringArguments.put("scope", Collections.singleton(String.join(" ", scopes)));
        queryStringArguments.put("acr_values", Collections.singleton(String.join(" ", acrValues)));

        _logger.debug("Redirecting to {} with query string arguments {}", AUTHORIZATION_ENDPOINT,
                queryStringArguments);

        throw _exceptionFactory.redirectException(AUTHORIZATION_ENDPOINT,
                RedirectStatusCode.MOVED_TEMPORARILY, queryStringArguments, false);
    }

    private void setAcrValues(Set<String> acrValues)
    {
        _config.getCountry().getSweden().ifPresent(options ->
        {
            options.isLoginOnOtherDevice().ifPresent(isOptionSelected ->
            {
                if (isOptionSelected)
                {
                    String acr = "urn:grn:authn:se:bankid:another-device";

                    _logger.debug("Adding ACR ({}) that will cause Criipto to perform Swedish BankID login on a " +
                            "different device", acr);

                    acrValues.add(acr);
                }
            });

            options.isLoginOnSameDevice().ifPresent(isOptionSelected ->
            {
                if (isOptionSelected)
                {
                    String acr = "urn:grn:authn:se:bankid:same-device";

                    _logger.debug("Adding ACR ({}) that will cause Criipto to perform Swedish BankID login on the " +
                            "same device", acr);

                    acrValues.add(acr);
                }
            });
        });

        _config.getCountry().getNorway().ifPresent(options ->
        {
            options.isLoginOnMobileDevice().ifPresent(isOptionSelected ->
            {
                if (isOptionSelected)
                {
                    String acr = "urn:grn:authn:no:bankid:mobile";

                    _logger.debug("Adding ACR ({}) that will cause Criipto to perform Norwegian BankID login on a " +
                            "mobile device", acr);

                    acrValues.add(acr);
                }
            });

            options.isLoginWithHardwareToken().ifPresent(isOptionSelected ->
            {
                if (isOptionSelected)
                {
                    String acr = "urn:grn:authn:no:bankid:central";

                    _logger.debug("Adding ACR ({}) that will cause Criipto to perform Norwegian BankID login using " +
                            "a hardware device", acr);

                    acrValues.add(acr);
                }
            });
        });

        _config.getCountry().getDenmark().ifPresent(options ->
        {
            String acr = "urn:grn:authn:dk:nemid:poces";

            _logger.debug("Adding ACR ({}) that will cause Criipto to perform Danish BankID login", acr);

            acrValues.add(acr);
        });
    }

    private String createRedirectUri()
    {
        try
        {
            URI authUri = _authenticatorInformationProvider.getFullyQualifiedAuthenticationUri();

            return new URL(authUri.toURL(), authUri.getPath() + "/" + CALLBACK).toString();
        } catch (MalformedURLException e)
        {
            throw _exceptionFactory.internalServerException(ErrorCode.INVALID_REDIRECT_URI,
                    "Could not create redirect URI");
        }
    }

    @Override
    public Optional<AuthenticationResult> post(Request request, Response response)
    {
        throw _exceptionFactory.methodNotAllowed();
    }

    @Override
    public Request preProcess(Request request, Response response)
    {
        return request;
    }
}
