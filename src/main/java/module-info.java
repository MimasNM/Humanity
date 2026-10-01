module org.example.humanity {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    /*
     * У mysql-connector-j нет module-info, поэтому Java выводит имя модуля
     * автоматически из имени файла: mysql-connector-j-9.4.0.jar -> mysql.connector.j
     * Если после смены версии драйвера сборка начнёт ругаться на "module not found",
     * посмотрите настоящее имя так:
     *   jar --describe-module --file ~/.m2/repository/com/mysql/mysql-connector-j/<версия>/mysql-connector-j-<версия>.jar
     * и подставьте его в строку ниже.
     */
    requires mysql.connector.j;

    opens org.example.humanity to javafx.fxml;
    opens org.example.humanity.controllers to javafx.fxml;

    /*
     * PropertyValueFactory достаёт значения геттерами через рефлексию из javafx.base,
     * поэтому models должен быть открыт именно ему, иначе все колонки таблиц будут пустыми
     * с IllegalAccessException в консоли.
     */
    opens org.example.humanity.models to javafx.base;

    exports org.example.humanity;
}
