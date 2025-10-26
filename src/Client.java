package task4;

public class Client {//客户端

    public static void main(String[] args) {
        try {
            //根据传入的参数选择file/database方法
            DataManipulation dm = new DataFactory().createDataManipulation(args[0]);
            //实现调用文件测试和 DBMS 测试的插入、查询、更新、删除函数，并打印测试结果
            //用户实行操作的语句
            //对应的输出
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}

