# Redis-Data
Redis-Data is designed to make saving your data in redis as easy as possible.
It uses the hash feature in redis along with GSON to make your development environment a lot easier to read.

# Example
For an example see [here](https://github.com/birthdates/Framework/tree/master/src/test/java/com/birthdates/redisdata/RedisDataTest.java)

# Data Structure
Each document being saved is of type `RedisDocument` (which can be managed with `RedisDataManager`)

# Maven
```xml
<dependency>
    <groupId>com.birthdates</groupId>
    <artifactId>redis-data</artifactId>
    <version>1.0.0</version>
</dependency>
```