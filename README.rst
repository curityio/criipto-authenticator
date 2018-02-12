Criipto Authenticator Plug-in
=============================

.. image:: https://travis-ci.org/curityio/criipto-authenticator.svg?branch=master
      :target: https://travis-ci.org/curityio/criipto-authenticator

This project provides an opens source Criipto Authenticator plug-in for the Curity Identity Server. This allows an administrator to add functionality to Curity which will then enable end users to login using their Criipto credentials. The app that integrates with Curity may also be configured to receive the Criipto access token, allowing it to manage resources in a Criipto.

System Requirements
~~~~~~~~~~~~~~~~~~~

* Curity Identity Server 2.4.0 and `its system requirements <https://developer.curity.io/docs/latest/system-admin-guide/system-requirements.html>`_

Requirements for Building from Source
"""""""""""""""""""""""""""""""""""""

* Maven 3
* Java JDK v. 8

Compiling the Plug-in from Source
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The source is very easy to compile. To do so from a shell, issue this command: ``mvn package``.

Installation
~~~~~~~~~~~~

To install this plug-in, either download a binary version available from the `releases section of this project's GitHub repository <https://github.com/curityio/criipto-authenticator/releases>`_ or compile it from source (as described above). If you compiled the plug-in from source, the package will be placed in the ``target`` subdirectory. The resulting JAR file or the one downloaded from GitHub needs to placed in the directory ``${IDSVR_HOME}/usr/share/plugins/criipto``. (The name of the last directory, ``criipto``, which is the plug-in group, is arbitrary and can be anything.) After doing so, the plug-in will become available as soon as the node is restarted.

.. note::

    The JAR file needs to be deployed to each run-time node and the admin node. For simple test deployments where the admin node is a run-time node, the JAR file only needs to be copied to one location.

The following JAR must also be added to the same plug-in, so that it is available in the class path of the plug-in. If it is not, a server error will result after login at Criipto with a log message in the server log saying that a certain class provided in that JAR could not be found:

* `jose4j-0.6.2.jar <http://central.maven.org/maven2/org/bitbucket/b_c/jose4j/0.6.2/jose4j-0.6.2.jar>`_

For a more detailed explanation of installing plug-ins, refer to the `Curity developer guide <https://developer.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation>`_.

Creating a Criipto Authenticator in Curity
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The easiest way to configure a new Criipto authenticator is using the Curity admin UI. The configuration for this can be downloaded as XML or CLI commands later, so only the steps to do this in the GUI will be described.

1. Go to the ``Authenticators`` page of the authentication profile wherein the authenticator instance should be created.
2. Click the ``New Authenticator`` button.
3. Enter a name (e.g., ``criipto1``). This name needs to match the URI component in the callback URI set in the Criipto app.
4. For the type, pick the ``Criipto`` option:

    .. figure:: docs/images/criipto-authenticator-type-in-curity.png
        :align: center
        :width: 600px

5. On the next page, you can define all of the standard authenticator configuration options like any previous authenticator that should run, the resulting ACR, transformers that should executed, etc. At the bottom of the configuration page, the Criipto-specific options can be found.

    .. note::

    The Criipto-specific configuration is generated dynamically based on the `configuration model defined in the Java interface <https://criipto.com/curityio/criipto-authenticator/blob/master/src/main/java/io/curity/identityserver/plugin/criipto/config/CriiptoAuthenticatorPluginConfig.java>`_.

6. Certain required and optional configuration settings may be provided. One of these is the ``HTTP Client`` setting. This is the HTTP client that will be used to communicate with the Criipto OAuth server's token and user info endpoints. To define this, do the following:

    A. click the ``Facilities`` button at the top-right of the screen.
    B. Next to ``HTTP``, click ``New``.
    C. Enter some name (e.g., ``criiptoClient``).
    D. Click ``Apply``.

        .. figure:: docs/images/criipto-http-client.png
                :align: center
                :width: 400px

7. Back in the Criipto authenticator instance that you started to define, select the new HTTP client from the dropdown.

        .. figure:: docs/images/http-client.png

8. In the ``Client ID`` textfield, enter the client ID from the Criipto app configuration.
9. Also enter the matching ``Client Secret``.
10. If you wish to limit the scopes that Curity will request of Criipto, select the desired scopes from dropdown.

Once all of these changes are made, they will be staged, but not committed (i.e., not running). To make them active, click the ``Commit`` menu option in the ``Changes`` menu. Optionally enter a comment in the ``Deploy Changes`` dialogue and click ``OK``.

Once the configuration is committed and running, the authenticator can be used like any other.

License
~~~~~~~

This plugin and its associated documentation is listed under the `Apache 2 license <LICENSE>`_.

More Information
~~~~~~~~~~~~~~~~

Please visit `curity.io <https://curity.io/>`_ for more information about the Curity Identity Server.

Copyright (C) 2017 Curity AB.
