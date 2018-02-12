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

package io.curity.identityserver.plugin.criipto.config;

import se.curity.identityserver.sdk.config.Configuration;
import se.curity.identityserver.sdk.config.OneOf;
import se.curity.identityserver.sdk.config.annotation.DefaultBoolean;
import se.curity.identityserver.sdk.config.annotation.Description;
import se.curity.identityserver.sdk.service.ExceptionFactory;
import se.curity.identityserver.sdk.service.HttpClient;
import se.curity.identityserver.sdk.service.Json;
import se.curity.identityserver.sdk.service.SessionManager;
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

        Optional<Sweden> getSweden();

        Optional<Norway> getNorway();

        Optional<Denmark> getDenmark();


        interface Sweden
        {
            LoginOnDevice getLoginOnDevice();

            interface LoginOnDevice extends OneOf
            {
                Optional<LoginOnSameDevice> isLoginOnSameDevice();

                Optional<LoginOnOtherDevice> isLoginOnOtherDevice();

                interface LoginOnSameDevice
                {
                    //TODO: Need to remove this statement, as the empty interface should work.
                    Optional<@DefaultBoolean(true) Boolean> isLoginOnSameDevice();
                }

                interface LoginOnOtherDevice
                {
                    //TODO: Need to remove this statement, as the empty interface should work.
                    Optional<@DefaultBoolean(true) Boolean> isLoginOnOtherDevice();
                }
            }

        }

        interface Norway
        {
            LoginOnDevice getLoginOnDevice();

            interface LoginOnDevice extends OneOf
            {
                Optional<LoginOnMobileDevice> isLoginOnMobileDevice();

                Optional<LoginWithHardwareToken> isLoginWithHardwareToken();

                interface LoginOnMobileDevice
                {
                    //TODO: Need to remove this statement, as the empty interface should work.
                    Optional<@DefaultBoolean(true) Boolean> isLoginOnMobileDevice();
                }

                interface LoginWithHardwareToken
                {
                    //TODO: Need to remove this statement, as the empty interface should work.
                    Optional<@DefaultBoolean(true) Boolean> isLoginWithHardwareToken();
                }
            }
        }

        interface Denmark
        {
            //TODO: Need to remove this statement, as the empty interface should work.
            Optional<@DefaultBoolean(true) Boolean> isDenmark();
        }
    }

    SessionManager getSessionManager();

    ExceptionFactory getExceptionFactory();

    AuthenticatorInformationProvider getAuthenticatorInformationProvider();

    WebServiceClientFactory getWebServiceClientFactory();

    Json getJson();
}
