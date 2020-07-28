package ir.simpletodo.data;

interface SQLiteConstants {

    String _db_name    = "SimpleTodoDb";
    int    _db_version = 3;

    String _table_task = "Task";
    String _table_important_tasks = "ImportantTasks";

    //Table Task Columns
    String _column_task_id = "Id";
    String _column_task_title = "Title";
    String _column_task_description = "Description";
    String _column_task_status = "Status";
    String _column_creation_date = "CreationDate";

    //Table Important Columns
    String _column_important_task_id = "TaskId";



}
