# aws-service-broker
[![Build Status](https://travis-ci.org/RawSanj/aws-service-broker.svg?branch=master)](https://travis-ci.org/RawSanj/aws-service-broker) [![Quality Gate](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=alert_status)](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=alert_status) [![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=reliability_rating)](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=reliability_rating) [![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=security_rating)](https://sonarcloud.io/api/project_badges/measure?project=com.github.rawsanj%3Aaws-service-broker&metric=security_rating) [![codecov](https://codecov.io/gh/RawSanj/aws-service-broker/branch/master/graph/badge.svg)](https://codecov.io/gh/RawSanj/aws-service-broker) [![Docker Build Status](https://img.shields.io/docker/build/jrottenberg/ffmpeg.svg)](https://hub.docker.com/r/rawsanj/aws-service-broker/) [![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
 
**aws-service-broker** is [Open Service Broker](https://www.openservicebrokerapi.org/) compatible API server that provisions managed services in AWS.

### Supported AWS Services

* [Amazon Relational Database Service (RDS)](https://aws.amazon.com/rds)
* [Amazon Simple Sorage Service (S3)](https://aws.amazon.com/s3)

### Build, Test and Run

#### Setup
```sh
$ git clone https://github.com/RawSanj/aws-service-broker.git

$ cd aws-service-broker
```

#### Configuration:
  - Create a new IAM user with Programmatic access (i.e. enable access key ID and secret access key for the AWS API) and attach following policies: AmazonRDSFullAccess, AmazonS3FullAccess and IAMFullAccess.
  - Add the above noted AWS Access key, Secret key and export them as environment variable (AWS_ACCESS_KEY, AWS_SECRET_KEY and AWS_DEFAULT_REGION).
  - Also Export Application Secret keys as environment variables.
```sh
$ // Export AWS Keys
$ export AWS_ACCESS_KEY=[YOUR_AWS_ACCESS_KEY]
$ export AWS_SECRET_KEY=[YOUR_AWS_SECRET_KEY]
$ export AWS_DEFAULT_REGION=[YOUR_AWS_DEFAULT_REGION]

$ // Export Application Secret Keys
$ export BROKER_APP_ADMIN_USERNAME=admin
$ export BROKER_APP_ADMIN_PASSWORD=p@$$w0rd
```

#### Build and Test
```sh
$ mvn clean package
```

#### Run the application:
```sh
$ java -jar aws-service-broker-[version]-exec.jar
```


### Tech

**aws-service-broker** uses a number of open source projects/spec:

* [Spring Boot] - An opinionated framework for building production-ready Spring applications. It favors convention over configuration and is designed to get you up and running as quickly as possible.
* [Open Service Broker API] - The Open Service Broker API project allows developers, ISVs, and SaaS vendors a single, simple, and elegant way to deliver services to applications running within cloud native platforms such as Cloud Foundry, OpenShift, and Kubernetes.
* [Spring Cloud Open Service Broker] - Spring Cloud Open Service Broker is a framework for building Spring Boot applications that implement the Open Service Broker API. 
* [Docker] - Docker is an open platform for developers and sysadmins to build, ship, and run distributed applications.
* [Cloudfoundry] - Cloud Foundry is the industry standard cloud application platform that abstracts away infrastructure so you can focus on app innovation.
* [Kubernetes] - Kubernetes is an open-source system for automating deployment, scaling, and management of containerized applications.


License
----

Apache License 2.0

Copyright (c) 2018 Sanjay Rawat

[//]: #

   [Spring Boot]:<https://projects.spring.io/spring-boot/>
   [Spring Cloud Open Service Broker]: <https://cloud.spring.io/spring-cloud-open-service-broker/>
   [Open Service Broker API]: <https://www.openservicebrokerapi.org>
   [Docker]: <https://www.docker.com>
   [Kubernetes]: <https://kubernetes.io>
   [Cloudfoundry]: <https://www.cloudfoundry.org>