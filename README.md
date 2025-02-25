# folio-spring-base

Copyright (C) 2020-2022 The Open Library Foundation

This software is distributed under the terms of the Apache License,
Version 2.0. See the file "[LICENSE](LICENSE)" for more information.

## Table of Contents

- [Table of Contents](#table-of-contents)
- [Introduction](#introduction)
- [Properties](#properties)
- [CQL support](#cql-support)
- [Logging](#logging)
  - [Default logging format](#default-logging-format)
  - [Logging for incoming and outgoing requests](#logging-for-incoming-and-outgoing-requests)
    - [Log examples:](#log-examples)
- [Custom `/_/tenant` Logic](#custom-_tenant-logic)
  - [`TenantService` Event Methods](#tenantservice-event-methods)
  - [`TenantService` Methods and Fields](#tenantservice-methods-and-fields)
  - [Event Order](#event-order)
    - [Upon Creation](#upon-creation)
    - [Upon Deletion](#upon-deletion)
  - [Sample](#sample)

## Introduction

This is a library (jar) that contains the basic functionality and main dependencies required for development of FOLIO modules using Spring framework (also known as "Spring Way").

## Properties

| Property                                              | Description                                                                                                                                                                                         | Default       | Example                      |
| ----------------------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- | ------------- | ---------------------------- |
| `header.validation.x-okapi-tenant.exclude.base-paths` | Specifies base paths to exclude form `x-okapi-tenant` header validation. See [TenantOkapiHeaderValidationFilter.java](src/main/java/org/folio/spring/filter/TenantOkapiHeaderValidationFilter.java) | `/admin`      | `/admin,/swagger-ui`         |
| `folio.jpa.repository.base-packages`                  | Specifies base packages to scan for repositories                                                                                                                                                    | `org.folio.*` | `org.folio.qm.dao`           |
| `folio.logging.request.enabled`                       | Turn on logging for incoming requests                                                                                                                                                               | `true`        | `true or false`              |
| `folio.logging.request.level`                         | Specifies logging level for incoming requests                                                                                                                                                       | `basic`       | `none, basic, headers, full` |
| `folio.logging.feign.enabled`                         | Turn on logging for outgoing requests in feign clients                                                                                                                                              | `true`        | `true or false`              |
| `folio.logging.feign.level`                           | Specifies logging level for outgoing requests                                                                                                                                                       | `basic`       | `none, basic, headers, full` |

## CQL support

To have ability to search entities in databases by CQL-queries:

- create repository interface for needed entity
- extend it from `JpaCqlRepository<T, ID>`, where `T` is entity class and `ID` is entity's id class.
- the implementation of the repository will be created by Spring

```java
public interface PersonRepository extends JpaCqlRepository<Person, Integer> {

}
```

Two methods are available for CQL-queries:

```java
public interface JpaCqlRepository<T, ID> extends JpaRepository<T, ID> {

  Page<T> findByCQL(String cql, OffsetRequest offset);

  long count(String cql);
}
```

## Logging

### Default logging format

Library uses [log4j2](https://logging.apache.org/log4j/2.x/) for logging. There are two default log4j2 configurations:

- `log4j2.properties` console/line based logger and it is the default
- `log4j2-json.properties` JSON structured logging

To choose the JSON structured logging by using setting: `-Dlog4j.configurationFile=log4j2-json.properties`
A module that wants to generate log4J2 logs in a different format can create a `log4j2.properties` file in the /resources directory.

### Logging for incoming and outgoing requests

By default, logging for incoming and outgoing request enabled. Module could disable it by setting:

- `folio.logging.request.enabled = false`
- `folio.logging.feign.enabled = false`

Also, it is possible to specify logging level:
`none` - no logs
`basic` - log request method and URI, response status and spent time
`headers` - log all that `basic` and request headers
`full` - log all that `headers` and request and response bodies

**_Note:_** _In case you have async requests in your module (DeferredResult, CompletableFuture, etc.) then you should disable default logging for requests._

#### Log examples:

- basic:

```text
18:41:18 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter ---> PUT /records-editor/records/c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1 null
18:41:19 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter <--- 202 in 753ms
```

- headers:

```text
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter ---> PUT /records-editor/records/c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1 null
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-url: http://localhost:50017
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-tenant: <tenantId>
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-request-id: <requestId>
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-user-id: <userId>
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter content-type: application/json; charset=UTF-8
18:44:23 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter ---> END HTTP
18:44:24 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter <--- 202 in 786ms
```

- full:

```text
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter ---> PUT /records-editor/records/c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1 null
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-url: http://localhost:53146
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-tenant: <tenantId>
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-request-id: <requestId>
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter x-okapi-user-id: <userId>
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter content-type: application/json; charset=UTF-8
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter Body: {"parsedRecordId":"c9db5d7a-e1d4-11e8-9f32-f2801f1b9fd1","parsedRecordDtoId":"c56b70ce-4ef6-47ef-8bc3-c470bafa0b8c","suppressDiscovery":false}
18:46:17 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter ---> END HTTP
18:46:18 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter <--- 202 in 714ms
18:46:18 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter Body:
18:46:18 [<requestId>] [<tenantId>] [<userId>] [<moduleId>] INFO  LoggingRequestFilter <--- END HTTP
```

## Custom `/_/tenant` Logic

There are many cases where you may want to add custom logic to the
[`/_/tenant` endpoint](https://s3.amazonaws.com/foliodocs/api/folio-spring-base/s/tenant.html),
such as for loading sample data or performing more complex database migration.

In order to do this, you can extend the `TenantService` within your module and override
any of the methods listed below.

### `TenantService` Event Methods

The following methods can be overridden by your module in order to add custom logic around events
relating to tenant creation, updates, and deletion. All of these return `void`.

Many of these accept a `TenantAttributes` parameter which can provide information about the
previous module (`module_from`), the module being upgraded to (`module_to`), as well as any other
`parameters` provided.

:warning: Please note that methods with "update" in the name will be run on updates _as well as_
when new tenants are created. Be especially careful with methods run before Liquibase -- the
database schema could potentially be in an unexpected state, particularly when a new tenant is
created.

| Visibility  | Signature                                 | Purpose                                                                                         |
| ----------- | ----------------------------------------- | ----------------------------------------------------------------------------------------------- |
| `public`    | `loadReferenceData()`                     | Load any reference data (requested with `loadReference=true` parameter)                         |
| `public`    | `loadSampleData()`                        | Load any sample data (requested with `loadSample=true` parameter)                               |
| `protected` | `beforeTenantUpdate(TenantAttributes)`    | Run custom logic before a tenant is created or updated                                          |
| `protected` | `beforeLiquibaseUpdate(TenantAttributes)` | Run custom logic immediately before Liquibase updates are started (after `beforeTenantUpdate`)  |
| `protected` | `afterLiquibaseUpdate(TenantAttributes)`  | Run custom logic immediately before Liquibase updates are finished (before `afterTenantUpdate`) |
| `protected` | `afterTenantUpdate(TenantAttributes)`     | Run custom logic after all update jobs are completed                                            |
| `protected` | `beforeTenantDeletion(TenantAttributes)`  | Run custom logic before a tenant is deleted/purged                                              |
| `protected` | `afterTenantDeletion(TenantAttributes)`   | Run custom logic after a tenant is deleted/purged (the schema will no longer exist)             |

### `TenantService` Methods and Fields

There are two methods that may be of use in your custom logic:

- `boolean tenantExists()` which will check if the database schema for this tenant exists (this
  says nothing about if it is up to date)
- `String getSchemaName()` will construct and return the name of the schema corresponding to the
  module and tenant

These fields will also be provided:

- `JdbcTemplate jdbcTemplate`, for running Postgres queries directly
- `FolioExecutionContext context`, for getting information about the module
- `FolioSpringLiquibase folioSpringLiquibase`, for interacting with Liquibase directly (this
  extends `SpringLiquibase` and may be `null` if Liquibase is not enabled!)

### Event Order

The [events](#tenantservice-event-methods) will be called in the following order:

#### Upon Creation

1. `beforeTenantUpdate`
2. If Liquibase is enabled:
   1. `beforeLiquibaseUpdate`
   2. _Internal logic to apply Liquibase changes_
   3. `afterLiquibaseUpdate`
3. `afterTenantUpdate`
4. `loadReferenceData`, if applicable
5. `loadSampleData`, if applicable

#### Upon Deletion

1. `beforeTenantDeletion`
2. _Internal logic to drop the schema_
3. `afterTenantDeletion`

### Sample

Overriding these methods to add your own custom logic is quite straightforward. Here is an example
of how to override these in your very own `@Service`:

```java
package org.folio.yourmodule.service;

import org.folio.spring.service.TenantService;
import org.folio.tenant.domain.dto.TenantAttributes;
import org.folio.yourmodule.SuperCoolDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service;

@Service
@Primary // required to ensure CustomTenantService will be loaded instead of TenantService
public class CustomTenantService extends TenantService {

  protected final SuperCoolDataRepository repository;

  /**
   * Load reference data
   */
  @Override
  protected void loadReferenceData() {
    repository.loadReferenceData();
  }

  /**
   * Add our custom initial data
   */
  @Override
  protected void beforeTenantUpdate(TenantAttributes attributes) {
    // some custom logic for potentially migrating data
  }
}
```
