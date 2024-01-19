# grpc-hang

How to start example
```sh
./gradlew generateProto
./gradlew run
```

How to check that the test can pass: set false to `final boolean enableRetry = true;` in Main.java test method and run again.

For detailed grpc logs uncomment `#io.grpc.level=FINE` in logging.properties file.
