#### script generates doc for placeholder with comments from yml

* build image - `docker build -t env-doc-generator .`
* run - `docker run --rm -v ${pwd}:/res env-doc-generator :path '"""/res/test.yml"""'`

**test.yml**
```
server:
  #server port
  #type: string
  port: ${SERVER_PORT}

app:
  #tmp folder
  tmp: ${TMP_FOLDER}
  #enable migration
  #type: bool
  enable: ${ENABLE_MIGRATION:false}
```

**test.yml.md**

|Name|Type|Default|Description|
|---|---|---|---|
| SERVER_PORT | string |  | server port |
| TMP_FOLDER |  |  | tmp folder |
| ENABLE_MIGRATION | bool | false | enable migration


