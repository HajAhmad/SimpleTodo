package ir.simpletodo.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.simpletodo.task.Task;

import static ir.simpletodo.AppConstants.STATUS_ACTIVE;
import static ir.simpletodo.AppConstants.STATUS_DONE;
import static ir.simpletodo.data.SQLiteConstants.*;

class DbTask extends SQLiteHelper {

    private static volatile DbTask sInstance;
    private static final Object _lock_object;

    private SQLiteDatabase mDb;

    static {
        _lock_object = new Object();
    }

    private DbTask(@NonNull Context context) {
        super(context, _db_name, _db_version);
    }

    static DbTask getInstance(@NonNull Context context) {
        Objects.requireNonNull(context);
        if (sInstance == null) {
            synchronized (_lock_object) {
                sInstance = new DbTask(context);
            }
        }
        return sInstance;
    }

    synchronized void openReadableDatabase() throws SQLException {
        if (!isOpen())
            mDb = getReadableDatabase();
    }

    synchronized void openWritableDatabase() throws SQLException {
        if (!isOpen())
            mDb = getWritableDatabase();
    }


    @Override
    public synchronized void close() {
        if (isOpen()) mDb.close();
        sInstance = null;
    }

    boolean isOpen() {
        return mDb != null && mDb.isOpen();
    }

    long insertTask(@NonNull Task task) {
        Objects.requireNonNull(task);
        ContentValues values = new ContentValues();
        values.put(_column_task_title, task.getTitle());
        values.put(_column_task_description, task.getDescription());
        values.put(_column_task_status, task.getStatus());
        values.put(_column_creation_date, task.getCreationDate());

        return mDb.insert(_table_task, null, values);
    }

    List<Task> getAllTasks() {
        Cursor c = mDb.rawQuery("select * from " + _table_task + " order by " +
                "datetime(" + _column_creation_date + ") DESC", null);

        List<Task> taskList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                Task task = new Task();
                task.setId(c.getLong(c.getColumnIndex(_column_task_id)));
                task.setTitle(c.getString(c.getColumnIndex(_column_task_title)));
                task.setDescription(c.getString(c.getColumnIndex(_column_task_description)));
                task.setStatus(c.getInt(c.getColumnIndex(_column_task_status)));

                taskList.add(task);
            } while (c.moveToNext());
            c.close();
        }

        return taskList;
    }

    Task getTask(long id) {
        Cursor c = mDb.query(_table_task, null, _column_task_id + " = ?",
                new String[]{String.valueOf(id)}, null, null, null);
        Task task = new Task();
        if (c != null && c.moveToFirst()) {
            task.setId(c.getLong(c.getColumnIndex(_column_task_id)));
            task.setTitle(c.getString(c.getColumnIndex(_column_task_title)));
            task.setDescription(c.getString(c.getColumnIndex(_column_task_description)));
            task.setStatus(c.getInt(c.getColumnIndex(_column_task_status)));
            task.setCreationDate(c.getString(c.getColumnIndex(_column_creation_date)));

            c.close();
        }

        return task;
    }

    int deleteTask(long taskId) {
        return mDb.delete(_table_task, _column_task_id + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    int updateTask(@NonNull Task task) {
        Objects.requireNonNull(task);
        ContentValues values = new ContentValues();
        values.put(_column_task_id, task.getId());
        values.put(_column_task_title, task.getTitle());
        values.put(_column_task_description, task.getDescription());
        values.put(_column_task_status, task.getStatus());
        return mDb.update(_table_task, values, _column_task_id + " = ?",
                new String[]{String.valueOf(task.getId())});
    }

    long insertImportantTask(long taskId) {
        ContentValues values = new ContentValues();
        if (!isTaskImportant(taskId)) {
            values.put(_column_important_task_id, taskId);
            mDb.insert(_table_important_tasks, null, values);
        }
        return taskId;
    }

    int deleteImportantTask(long taskId) {
        return mDb.delete(_table_important_tasks, _column_important_task_id + " = ?",
                new String[]{String.valueOf(taskId)});
    }

    boolean isTaskImportant(long taskId) {
        Cursor c = mDb.query(_table_important_tasks, new String[]{_column_important_task_id},
                _column_important_task_id + " = ?", new String[]{String.valueOf(taskId)},
                null, null, null, "1");
        if (c != null && c.moveToFirst()) {
            c.close();
            return true;
        } else {
            if (c != null) c.close();
            return false;
        }
    }

    List<Task> getAllImportantTaskList() {
        Cursor c = mDb.query(_table_important_tasks, new String[]{_column_important_task_id},
                null, null, null, null, null);

        List<Task> importantTaskList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                long importantTaskId = c.getLong(0);
                Task task = getTask(importantTaskId);
                task.setImportant(true);
                importantTaskList.add(task);
            } while (c.moveToNext());
            c.close();
        }

        return importantTaskList;
    }


    List<Task> getDoneTaskList() {
        Cursor c = mDb.query(_table_task, null, _column_task_status + " = ?",
                new String[]{String.valueOf(STATUS_DONE)}, null,
                null, null);
        List<Task> doneTaskList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                doneTaskList.add(new Task(
                        c.getLong(c.getColumnIndex(_column_task_id)),
                        c.getString(c.getColumnIndex(_column_task_title)),
                        c.getString(c.getColumnIndex(_column_task_description)),
                        c.getInt(c.getColumnIndex(_column_task_status))
                ));
            } while (c.moveToNext());
            c.close();
        }

        return doneTaskList;
    }

    List<Task> getActiveTaskList() {
        Cursor c = mDb.query(_table_task, null, _column_task_status + " = ?",
                new String[]{String.valueOf(STATUS_ACTIVE)}, null,
                null, null);
        List<Task> activeTaskList = new ArrayList<>();
        if (c != null && c.moveToFirst()) {
            do {
                activeTaskList.add(new Task(
                        c.getLong(c.getColumnIndex(_column_task_id)),
                        c.getString(c.getColumnIndex(_column_task_title)),
                        c.getString(c.getColumnIndex(_column_task_description)),
                        c.getInt(c.getColumnIndex(_column_task_status))
                ));
            } while (c.moveToNext());
            c.close();
        }

        return activeTaskList;
    }

    public long getTasksTableCount() {
        return DatabaseUtils.queryNumEntries(mDb, _table_task);
    }
}
