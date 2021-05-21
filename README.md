# Redis-Data
Redis-Data is designed to make saving your data in redis as easy as possible.
It uses the hash feature in redis along with GSON to make your development environment a lot easier to read.

# Example
For an example see [here](https://github.com/birthdates/Redis-Data/tree/master/src/test/java/com/birthdates/redisdata/RedisDataTest.java)

# Data Structure
Each document being saved is of type `RedisDocument` (which can be managed with `RedisDataManager`)

# Document
The `RedisDocument` alone is abstract, so you will have to implement a few functions:
```java
public abstract String getNamespace();
public abstract String getId();
public abstract Class<?> getType();
```
The class comes with four data functions `save`, `load`, `delete` & `expire`. When you load a document you can check if it's new with `isNew()`

# Maven
```xml
<dependency>
    <groupId>com.birthdates</groupId>
    <artifactId>redis-data</artifactId>
    <version>1.0.3</version>
</dependency>
```