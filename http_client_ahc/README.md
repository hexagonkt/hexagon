
# Module http_client_ahc

[port_http_client] implementation using the [Async HTTP Client] library.

[port_http_client]: /port_http_client
[Async HTTP Client]: https://github.com/AsyncHttpClient/async-http-client

### Install the Dependency

=== "build.gradle"

    ```groovy
    repositories {
        mavenCentral()
    }

    implementation("com.hexagonkt:http_client_ahc:$hexagonVersion")
    ```

=== "pom.xml"

    ```xml
    <dependency>
      <groupId>com.hexagonkt</groupId>
      <artifactId>http_client_ahc</artifactId>
      <version>$hexagonVersion</version>
    </dependency>
    ```

# Package com.hexagonkt.http.client.ahc

Async HTTP client implementation classes.
