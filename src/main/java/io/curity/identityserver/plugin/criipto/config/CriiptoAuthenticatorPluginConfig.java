/*
 *  Copyright 2018 Curity AB
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

package io.curity.identityserver.plugin.criipto.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.OneOf;
import se.curity.identityserver.sdk.config.annotation.DefaultBoolean;
import se.curity.identityserver.sdk.config.annotation.DefaultEnum;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
import se.curity.identityserver.sdk.service.UserPreferenceManager;
import se.curity.identityserver.sdk.service.WebServiceClientFactory;
import se.curity.identityserver.sdk.service.authentication.AuthenticatorInformationProvider;

import java.util.Optional;

@SuppressWarnings("InterfaceNeverImplemented")
public interface CriiptoAuthenticatorPluginConfig extends Configuration
{

    @Description("The Client ID/realm of the application configured in Criipto for Curity")
    String getClientId();

    @Description("The associated secret for the client configured for Curity in Criipto")
    String getClientSecret();

    @Description("The HTTP client with any proxy and TLS settings that will be used to connect to Criipto")
    Optional<HttpClient> getHttpClient();

    @Description("The domain or tenant ID at Criipto")
    String getDomain();

    Country getCountry();

    interface Country extends OneOf
    {
        @Description("Login using Swedish BankID")
        Optional<Sweden> getSweden();

        @Description("Login using Norwegian BankID")
        Optional<Norway> getNorway();

        @Description("Login using NemID")
        Optional<Denmark> getDenmark();
        
        interface Sweden
        {
            @Description("How the user should login -- either on the same device or another device")
            @DefaultEnum("SAME_DEVICE")
            LoginUsing getLoginUsing();

            enum LoginUsing
            {
                @Description("Login on the same device")
                SAME_DEVICE,

                @Description("Login on some other device")
                OTHER_DEVICE
            }
        }

        interface Norway
        {
            @Description("How the user should login -- either on a mobile device or a hardware token")
            @DefaultEnum("MOBILE_DEVICE")
            LoginUsing getLoginUsing();

            enum LoginUsing
            {
                @Description("Login on a mobile device")
                MOBILE_DEVICE,

                @Description("Login using a hardware token")
                HARDWARE_TOKEN
            }
        }

        interface Denmark
        {
            @DefaultBoolean(true)
            boolean isDanish();
        }
    }

    UserPreferenceManager getUserPreferenceManager();

    SessionManager getSessionManager();

    ExceptionFactory getExceptionFactory();

    AuthenticatorInformationProvider getAuthenticatorInformationProvider();

    WebServiceClientFactory getWebServiceClientFactory();

    Json getJson();
}
