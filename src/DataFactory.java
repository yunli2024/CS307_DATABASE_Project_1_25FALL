package task4;//照抄，已完成
//用于处理通过反射调用方法时可能抛出的异常
import java.lang.reflect.InvocationTargetException;

public class DataFactory {//一个工厂类，用于创建数据操作对象
    public DataManipulation createDataManipulation(String arg) {//返回类型为DataManipulation接口
        String name;
        if (arg.toLowerCase().contains("file")) {
            name = "FileManipulation";
        } else if (arg.toLowerCase().contains("database")) {
            name = "DatabaseManipulation";
        } else {
            throw new IllegalArgumentException("Illegal Argument:" + arg);
        }
        try {//用于捕获反射操作可能抛出的异常
            return (DataManipulation) Class.forName(name).getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
