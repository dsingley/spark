package spark;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

class QueryParamsMapTest {
    
    @Test
    void constructorWithParametersMap() {
        Map<String,String[]> params = new HashMap<>();
        
        params.put("user[info][name]",new String[] {"fede"});

        var queryMap = new QueryParamsMap(params);
        
        assertAll(
                () -> assertThat(queryMap.get("user").get("info").get("name").value()).isEqualTo("fede"),
                () -> assertThat(queryMap.get("user","info","name").value()).isEqualTo("fede")
        );
    }
    
    @Test
    void keyToMap() {
        var queryMap = new QueryParamsMap();
        
        queryMap.loadKeys("user[info][first_name]",new String[] {"federico"});
        queryMap.loadKeys("user[info][last_name]",new String[] {"dayan"});

        assertThat(queryMap.getQueryMap()).isNotEmpty();

        assertAll(
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap()).isNotEmpty(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("info").getQueryMap()).isNotEmpty(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("info").getQueryMap().get("first_name").getValues()[0]).isEqualTo("federico"),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("info").getQueryMap().get("last_name").getValues()[0]).isEqualTo("dayan"),
                () -> assertThat(queryMap.hasKey("user")).isTrue(),
                () -> assertThat(queryMap.hasKey("frame")).isFalse(),
                () -> assertThat(queryMap.hasKey(null)).isFalse(),
                () -> assertThat(queryMap.hasKeys()).isTrue(),
                () -> assertThat(queryMap.hasValue()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("info").getQueryMap().get("last_name").hasValue()).isTrue()
        );
    }
    
    @Test
    void testDifferentTypesForValue() {
        QueryParamsMap queryMap = new QueryParamsMap();
        
        queryMap.loadKeys("user[age]",new String[] {"10"});
        queryMap.loadKeys("user[agrees]",new String[] {"true"});

        assertAll(
                () -> assertThat(queryMap.get("user").get("age").integerValue()).isEqualTo(10),
                () -> assertThat(queryMap.get("user").get("age").floatValue()).isEqualTo(10f),
                () -> assertThat(queryMap.get("user").get("age").doubleValue()).isEqualTo(10d),
                () -> assertThat(queryMap.get("user").get("age").longValue()).isEqualTo(10L),
                () -> assertThat(queryMap.get("user").get("agrees").booleanValue()).isEqualTo(Boolean.TRUE)
        );
    }
    
    @Test
    void parseKeyShouldParseRootKey() {
        var queryMap = new QueryParamsMap();
        String[] parsed = queryMap.parseKey("user[name][more]");
        assertThat(parsed).isNotNull();

        assertAll(
                () -> assertThat(parsed[0]).isEqualTo("user"),
                () -> assertThat(parsed[1]).isEqualTo("[name][more]")
        );
    }
    
    @Test
    void parseKeyShouldParseSubkeys() {
        var queryMap = new QueryParamsMap();
        String[] parsedNameMore = queryMap.parseKey("[name][more]");
        assertThat(parsedNameMore).isNotNull();

        assertAll(
                () -> assertThat(parsedNameMore[0]).isEqualTo("name"),
                () -> assertThat(parsedNameMore[1]).isEqualTo("[more]")
        );

        String[] parsedMore = queryMap.parseKey("[more]");
        assertThat(parsedMore).isNotNull();

        assertAll(
                () -> assertThat(parsedMore[0]).isEqualTo("more"),
                () -> assertThat(parsedMore[1]).isEmpty()
        );
    }
    
    @Test
    void itShouldBeNullSafe() {
        var queryParamsMap = new QueryParamsMap();
        
        String ret = queryParamsMap.get("x").get("z").get("y").value("w");
        
        assertThat(ret).isNull();
    }
    
    @Test
    void testConstructor() {
        var queryMap = new QueryParamsMap("user[name][more]","fede");

        assertThat(queryMap.getQueryMap()).isNotEmpty();
        assertAll(
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap()).isNotEmpty(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("name").getQueryMap()).isNotEmpty(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("name").getQueryMap().get("more").getValues()[0]).isEqualTo("fede")
        );
    }
    
    @Test
    void testToMap() {
        Map<String,String[]> params = new HashMap<>();
        
        params.put("user[info][name]",new String[] {"fede"});
        params.put("user[info][last]",new String[] {"dayan"});

        var queryMap = new QueryParamsMap(params);
        
        Map<String,String[]> map = queryMap.get("user","info").toMap();

        assertThat(map).hasSize(2);
        assertAll(
                () -> assertThat(map.get("name")[0]).isEqualTo("fede"),
                () -> assertThat(map.get("last")[0]).isEqualTo("dayan")
        );
    }
    
    
}
