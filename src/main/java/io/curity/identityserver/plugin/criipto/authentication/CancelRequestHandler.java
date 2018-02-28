package io.curity.identityserver.plugin.criipto.authentication;

import io.curity.identityserver.plugin.criipto.config.CriiptoAuthenticatorPluginConfig;
import se.curity.identityserver.sdk.authentication.AuthenticationResult;
import se.curity.identityserver.sdk.authentication.AuthenticatorRequestHandler;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;
import se.curity.identityserver.sdk.web.Request;
import se.curity.identityserver.sdk.web.Response;

import java.util.Optional;

public class CancelRequestHandler implements AuthenticatorRequestHandler<Request>
{
    private final ExceptionFactory _exceptionFactory;
    private final AuthenticatorInformationProvider _authenticatorInformationProvider;

    public CancelRequestHandler(CriiptoAuthenticatorPluginConfig config)
    {
        _exceptionFactory = config.getExceptionFactory();
        _authenticatorInformationProvider = config.getAuthenticatorInformationProvider();
    }

    @Override
    public Optional<AuthenticationResult> get(Request request, Response response)
    {
        throw _exceptionFactory.redirectException(
                _authenticatorInformationProvider.getAuthenticationBaseUri().toASCIIString());
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
