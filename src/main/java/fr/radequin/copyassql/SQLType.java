package fr.radequin.copyassql;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum SQLType {

    INTEGER("INTEGER", x -> x),
    DECIMAL("DECIMAL", x -> x),
    SMALLINT("SMALLINT", x -> x),
    VARCHAR("VARCHAR", x -> '\'' + x + '\''),
    TIMESTAMP("TIMESTAMP", x -> '\'' + x + '\''),
    CHARACTER("CHARACTER", x -> '\'' + x + '\'');


    String type;
    Function<String, String> fun;


    SQLType(String type, Function<String, String> fun) {
        this.type = type;
        this.fun = fun;
    }


    private static String apply (String value, Function<String, String> fun){
        return fun.apply(value.replaceAll("'", "''"));
    }
    private static String applyDefault (String value, Function<String, String> fun){
        return fun.apply(value.replaceAll("'", "''"));
    }


    private static Optional<SQLType> getByValue(String columnType) {
        return Arrays.stream(SQLType.values()).filter(en ->  en.type.equalsIgnoreCase(columnType)).findFirst();
    }

    public static String apply(String value, String columnType){
        return getByValue(columnType).map(x -> apply(value, x.fun)).orElse(applyDefault(value, x -> '\'' + x + '\''));

    }
}
