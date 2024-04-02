
# kv 转换工具
通过本地缓存一些字典或自定义转换的数据，达到查key回显value的效果。
## 使用方式：
1、定义转换方法，在service中实现IKVService接口
```java
@KVName("approveType")
public class ApproveTypeNameKVServiceImpl implements IKVService<DefaultCacheObject> {
    //此处可以是其他rpc调用，或者单纯的Enum枚举类提供基础数据
    @DubboReference
    private ApproveRulesService approveRulesService;

    //此处是为了项目启动时加载相关字典数据做到初始化用的
    @Override
    public List<DefaultCacheObject> list() {
        return new ArrayList<>();
    }

    @Override
    public DefaultCacheObject select(String s) {
        BaseCacheObject bo = getObject(s);
        try {
            if (StringUtils.isEmpty(bo.getKVkey())) {
                return (DefaultCacheObject) bo;
            }
            Long hosId = StringUtils.isEmpty(bo.getKVhosId()) ? null : Long.valueOf(bo.getKVhosId());
            List<ApproveRules> approveRules = approveRulesService.selectRulesByTypeAndHosId(bo.getKVkey(), hosId);
            if (CollectionUtils.isNotEmpty(approveRules)) {
                bo.setKVvalue(approveRules.get(0).getTypeName());
            }
        } catch (Exception e) {
            log.error("KV " + this.getClass() + " Info error:", e);
        }
        return (DefaultCacheObject) bo;
    }
}
```
通过注解定义服务名“approveType”，并实现缓存的获取数据方式

2、在DO对象上加上注解 '@KVParam'
其中 'name'是之前定义的服务名，带relate的会去取属性值对应的字段的值，没有的就直接取设置的值
```java
    @KVParam(name = "approveType", relateHosId = "hosId", relateValue = "examBody")
    private String examBodyName;
```
3、通过KVUtil暴露的方法（如 KVUtil.convertKV(Object o)）达到将Object o对象中数据进行转换
4、对使用mybatis的项目，通过@EnableMybatis启动mybatis插件，查询数据后自动调用kv组件进行kv转换达到数据库查到为“3”但是对象被赋值为字典值“已通过”的效果


