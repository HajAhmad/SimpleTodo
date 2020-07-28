package ir.simpletodo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static ir.simpletodo.data.SQLiteConstants.*;

import androidx.annotation.Nullable;

class SQLiteHelper extends SQLiteOpenHelper {

    SQLiteHelper(@Nullable Context context, @Nullable String name, int version) {
        super(context, name, null, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Table Tasks creation statement.
        db.execSQL("create table " + _table_task + " (" +
                _column_task_id + " integer primary key autoincrement," +
                _column_task_title + " text not null," +
                _column_task_description + " text," +
                _column_task_status + " integer," +
                _column_creation_date + " text" +
                ");");

        //Table ImportantTasks creation statement.
        db.execSQL("create table " + _table_important_tasks + " (" +
                _column_important_task_id + " integer primary key" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists " + _table_task);
        db.execSQL("drop table if exists " + _table_important_tasks);

        onCreate(db);
    }
}
