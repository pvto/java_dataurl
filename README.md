# java_dataurl
A simple implementation for handling data urls in Java.

##Dependencies:
###Java 1.5>
###Apache commons-codec (org.apache.commons.codec.binary.Base64)


##Usage:

```
  Dataurl dataurl = Dataurl.parse(string);
```

```
  Dataurl dataurl = Dataurl.parse(inputStream);
```

```
  String str = dataurl.getDataUrlString();
```

