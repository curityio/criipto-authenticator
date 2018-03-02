Criipto Authenticator Plug-in
=============================

.. image:: https://travis-ci.org/curityio/criipto-authenticator.svg?branch=master
      :target: https://travis-ci.org/curityio/criipto-authenticator

This project provides an opens source Criipto Authenticator plug-in for the Curity Identity Server. This allows an administrator to add functionality to Curity which will then enable end users to login using their Criipto credentials. The app that integrates with Curity may also be configured to receive the Criipto access token, allowing it to manage resources in a Criipto.

System Requirements
~~~~~~~~~~~~~~~~~~~

* Curity Identity Server 2.4.0 and `its system requirements <https://developer.curity.io/docs/latest/system-admin-guide/system-requirements.html>`_. Configuration of all settings in the GUI requires version 3.0.0.

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

For a more detailed explanation of installing plug-ins, refer to the `Curity developer guide <https://developer.curity.io/docs/latest/developer-guide/plugins/index.html#plugin-installation>`_.

Creating a Criipto Authenticator in Curity
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Configuration using the Admin GUI
"""""""""""""""""""""""""""""""""

To configure a new Criipto authenticator using the Curity admin UI, do the following after logging in:

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

7. Back in the Criipto authenticator instance that you started to define, select the new HTTP client from the dropdown.
8. In the ``Client ID`` textfield, enter the client ID from the Criipto app configuration.
9. Also enter the matching ``Client Secret``.
10. Specify the domain to be used in the ``Domain`` textfield. This will be the subdomain of ``criipto.id`` or similar.
11. Choose the country's who's E-ID type should be used. For instance, choose ``sweden`` to use Swedish BankID, ``denmark`` for NemID, and ``norway`` for Norwegian Bank ID.
10. If ``sweden`` or ``norway`` are chosen, then another dropdown will be displayed allowing for the configuration of authentication using the same device or a different device in the case of Sweden or hardware token or mobile phone in the case of Norway.

Once all of these changes are made, they will be staged, but not committed (i.e., not running). To make them active, click the ``Commit`` menu option in the ``Changes`` menu. Optionally enter a comment in the ``Deploy Changes`` dialogue and click ``OK``.

Once the configuration is committed and running, the authenticator can be used like any other.

Configuration using the CLI
"""""""""""""""""""""""""""

It is very common to create many instances of the Criipto authenticator. For example, you may configure one for Swedish BankID using the same device and another for different devices. These may be put into a group or combined with other authenticators to provide a suite of Scandinavian E-IDs. This can be tedious to configure in the UI. Also, due to a bug in version 2.4.0 of the GUI, it was not possible to configure Danish NemID. For these reasons, you may want to use the CLI to configure instances of this authenticator instead. Doing so is very easy:

1. Start the ``idsh`` command (located in ``$IDSVR_HOME/bin``)
2. Enter configuration mode by typing ``configure`` and hitting Enter.
3. Next, define the new Criipto authenticator instance in some authentication profile. For example, if the authentication profile is called ``for``, then the following commands would create the authenticator:

    .. code-block::

        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 description "Other Device"
        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 criipto
        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 criipto client-id urn:easyid:1
        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 criipto client-secret QWxhZGRpbjpvcGVuIHNlc2FtZQ==
        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 criipto sweden login-using other-device
        set profiles profile foo authentication-service settings authentication-service authenticators authenticator criipto1 criipto domain example.criipto.id

4. After you have defined the Criipto authenticator instance, type ``commit`` to make the configuration active.
5. Exit the shell by typing ``exit`` followed by Enter two times or just hit ``Ctrl-D`` a couple times.

For more information on the CLI, refer to this `introductory video <https://developer.curity.io/videos/video/cli-introduction>`_.

License
~~~~~~~

This plugin and its associated documentation is listed under the `Apache 2 license <LICENSE>`_.

More Information
~~~~~~~~~~~~~~~~

Please visit `curity.io <https://curity.io/>`_ for more information about the Curity Identity Server.

Copyright (C) 2018 Curity AB.
