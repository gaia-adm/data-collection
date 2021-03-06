GAIA on-premise agent
=====================

GAIA on-premise agent is a component that performs collection of data
from on-premise systems and sends it to GAIA analytics on cloud.

Quick Start Linux
=================

1. Unpack the distribution archive

2. Configure agent, providers and credentials in ./conf directory

3. Run GaiaAgent.sh

Quick Start Windows
===================

1. Unpack the distribution archive

2. Configure agent, providers and credentials in ./conf directory

3. Run GaiaAgent.bat

Configuration
=============

Sample agent.json configuration file:

{
  /* Location of GAIA result upload service */
  "gaiaLocation": "http://result-upload-service:9006",
  /* OAuth 2.0 Access token for authentication to GAIA. Use "plain" to keep the value unencrypted or "encrypt" to have it encrypted. */
  //"accessToken": {"encrypt": "991c94a9-0f20-4f63-9593-cb2af9760937"},
  /* Worker pool determines how many data collections can execute in parallel */
  "workerPool": 2,
  /* Socket read timeout in milliseconds */
  "soTimeout": 60000,
  /* Connect timeout in milliseconds when establishing connection to GAIA */
  "connectTimeout": 30000
}

"gaiaLocation" and "accessToken" are required. If "accessToken" is defined as "encrypt", new agent.json.encrypted file
will be created during runtime where the value will be encrypted. This new file can then be used to replace the original file.
Defining "accessToken" with "plain" means the value is not to be encrypted.

Sample credentials.json configuration file:

[
  // Sample credential definition. Value of "credentialsId" must match value used in providers.json. Credential values are provider specific.
  {
    "credentialsId": "sampleCredentialsId",
    "values": {
      "password": {
        "encrypt": "samplePassword"
      },
      "username": {
        "plain": "sampleUsername"
      }
    }
  }
]

credentials.json is used as a credential store for providers. Value of "credentialsId" must match value in providers.json.
Values of credentials - "username", "password" etc. are implementation specific and each may be encrypted.
If at least one credentials value is defined as "encrypt", new file credentials.json.encrypted will be created during runtime
that may be used to replace the original file.

Sample providers.json configuration file:

{
  "providers": [
    {
      // Unique provider configuration id. It may be a number or string.
      "configId": "1",
      // Id of provider this configuration is for. List of allowed providerIds depends on bundled providers. See DataProvider implementations.
      "providerId": "dummy",
      // Provider specific configuration properties, defined by DataProvider implementation.
      "properties": {
        "location": "http://localhost:8080/qcbin",
        "project": "ProjectName",
        "domain": "DomainName"
      },
      // Credentials to use for connecting to data source. Value of "credentialsId" must match value in credentials.json.
      "credentialsId": "dummyCredentialsId",
      // Provider specific proxy. Use only if provider needs to use separate proxy from other providers.
      "proxy": {
        "httpProxy": "http://proxy.bbn.hp.com:8080",
        "httpProxyUser": "user",
        "httpProxyPassword": {"plain": "pass"}
      },
      // Run period in minutes
      "runPeriod": 15
    }
  ],
  // Global proxy for all providers that do not specify one. Usually only one global "httpProxy needs to be specified.
  "proxy": {
    "httpProxy": "http://proxy.bbn.hp.com:8080",
    "httpProxyUser": "user",
    // password for httpProxy. Use "plain" to keep it plaintext, "encrypt" to have it encrypted
    "httpProxyPassword": {"encrypt": "pass"}
  }
}

provider.json is used to configure bundled data providers for data collection. Each configuration must have a unique
"configId" which may be a number or string - identifying what is being collected. "providerId" must reference an existing
DataProvider implementation. It is recommended to use only one global proxy instead of per provider proxy when possible.
Proxy server passwords may be encrypted.  If at least one proxy password value is defined as "encrypt", new file
providers.json.encrypted will be created during runtime that may be used to replace the original file.


Running GAIA on-premise agent
=============================

GAIA on-premise agent requires Java 8 to be available on the system. Java executable must be available
on path.
