package spark;

import java.util.HashMap;

import java.util.Map;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

public class QueryParamsMapTest {

    QueryParamsMap queryMap = new QueryParamsMap();
    
    @Test
    public void constructorWithParametersMap() {
        Map<String,String[]> params = new HashMap<>();
        
        params.put("user[info][name]",new String[] {"fede"});
        
        QueryParamsMap queryMap = new QueryParamsMap(params);
        
        assertAll(
                () -> assertThat(queryMap.get("user").get("info").get("name").value()).isEqualTo("fede"),
                () -> assertThat(queryMap.get("user","info","name").value()).isEqualTo("fede")
        );
    }
    
    @Test
    public void keyToMap() {
        QueryParamsMap queryMap = new QueryParamsMap();
        
        queryMap.loadKeys("user[info][first_name]",new String[] {"federico"});
        queryMap.loadKeys("user[info][last_name]",new String[] {"dayan"});

        assertAll(
                () -> assertThat(queryMap.getQueryMap().isEmpty()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().isEmpty()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("info").getQueryMap().isEmpty()).isFalse(),
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
    public void testDifferentTypesForValue() {
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
    public void parseKeyShouldParseRootKey() {
        String[] parsed = queryMap.parseKey("user[name][more]");
        
        assertAll(
                () -> assertThat(parsed[0]).isEqualTo("user"),
                () -> assertThat(parsed[1]).isEqualTo("[name][more]")
        );
    }
    
    @Test
    public void parseKeyShouldParseSubkeys() {
        String[] parsedNameMore = queryMap.parseKey("[name][more]");

        assertAll(
                () -> assertThat(parsedNameMore[0]).isEqualTo("name"),
                () -> assertThat(parsedNameMore[1]).isEqualTo("[more]")
        );

        String[] parsedMore = queryMap.parseKey("[more]");

        assertAll(
                () -> assertThat(parsedMore[0]).isEqualTo("more"),
                () -> assertThat(parsedMore[1]).isEqualTo("")
        );
    }
    
    @Test
    public void itShouldbeNullSafe() {
        QueryParamsMap queryParamsMap = new QueryParamsMap();
        
        String ret = queryParamsMap.get("x").get("z").get("y").value("w");
        
        assertThat(ret).isNull();
    }
    
    @Test
    public void testConstructor() {
        QueryParamsMap queryMap = new QueryParamsMap("user[name][more]","fede");

        assertAll(
                () -> assertThat(queryMap.getQueryMap().isEmpty()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().isEmpty()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("name").getQueryMap().isEmpty()).isFalse(),
                () -> assertThat(queryMap.getQueryMap().get("user").getQueryMap().get("name").getQueryMap().get("more").getValues()[0]).isEqualTo("fede")
        );
    }
    
    @Test
    public void testToMap() {
        Map<String,String[]> params = new HashMap<>();
        
        params.put("user[info][name]",new String[] {"fede"});
        params.put("user[info][last]",new String[] {"dayan"});
        
        QueryParamsMap queryMap = new QueryParamsMap(params);
        
        Map<String,String[]> map = queryMap.get("user","info").toMap();
        
        assertAll(
                () -> assertThat(map.size()).isEqualTo(2),
                () -> assertThat(map.get("name")[0]).isEqualTo("fede"),
                () -> assertThat(map.get("last")[0]).isEqualTo("dayan")
        );
    }
    
    
}
