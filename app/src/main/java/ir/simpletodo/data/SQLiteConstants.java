package ir.simpletodo.data;

class SQLiteConstants {

    public static final String _db_name = "SimpleTodoDb";
    public static final int _db_version = 3;

    public static final String _table_task = "Task";
    public static final String _table_important_tasks = "ImportantTasks";

    //Table Task Columns
    public static final String _column_task_id = "Id";
    public static final String _column_task_title = "Title";
    public static final String _column_task_description = "Description";
    public static final String _column_task_status = "Status";
    public static final String _column_creation_date = "CreationDate";

    //Table Important Columns
    public static final String _column_important_task_id = "TaskId";


}
