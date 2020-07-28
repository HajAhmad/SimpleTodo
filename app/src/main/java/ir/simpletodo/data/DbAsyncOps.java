package ir.simpletodo.data;

import android.content.Context;

import java.util.List;

import ir.simpletodo.task.Task;

public final class DbAsyncOps {

    private DbAsyncOps() {
    }

    public static class GetAllTaskList extends BaseAsyncTask<Void, Void, List<Task>> {
        public GetAllTaskList(Context context, OnFinishListener<List<Task>> listener) {
            super(context, listener);
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            List<Task> list = getReadableDbTask().getAllTasks();
            for (Task task : list)
                task.setImportant(getReadableDbTask().isTaskImportant(task.getId()));
            close();
            return list;
        }
    }

    public static class GetTasksCount extends BaseAsyncTask<Void, Void, Long> {
        public GetTasksCount(Context context, OnFinishListener<Long> listener) {
            super(context, listener);
        }

        @Override
        protected Long doInBackground(Void... voids) {
            long tableCount = getReadableDbTask().getTasksTableCount();
            close();
            return tableCount;
        }
    }

    public static class GetAllDoneTaskList extends BaseAsyncTask<Void, Void, List<Task>> {
        public GetAllDoneTaskList(Context context, OnFinishListener<List<Task>> listener) {
            super(context, listener);
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            List<Task> doneTaskList = getReadableDbTask().getDoneTaskList();
            close();
            return doneTaskList;
        }
    }

    public static class GetAllActiveTaskList extends BaseAsyncTask<Void, Void, List<Task>> {
        public GetAllActiveTaskList(Context context, OnFinishListener<List<Task>> list) {
            super(context, list);
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            List<Task> activeTaskList = getReadableDbTask().getActiveTaskList();
            close();
            return activeTaskList;
        }
    }

    public static class GetTask extends BaseAsyncTask<Long, Void, Task> {
        public GetTask(Context context, OnFinishListener<Task> listener) {
            super(context, listener);
        }

        @Override
        protected Task doInBackground(Long... longs) {
            Task task = getReadableDbTask().getTask(longs[0]);
            close();
            return task;
        }
    }

    public static class InsertTask extends BaseAsyncTask<Task, Void, Long> {
        public InsertTask(Context context, OnFinishListener<Long> listener) {
            super(context, listener);
        }

        @Override
        protected Long doInBackground(Task... tasks) {
            long l = getWritableDbTask().insertTask(tasks[0]);
            close();
            return l;
        }
    }

    public static class DeleteTask extends BaseAsyncTask<Long, Void, Integer> {
        public DeleteTask(Context context, OnFinishListener<Integer> listener) {
            super(context, listener);
        }

        @Override
        protected final Integer doInBackground(Long... ids) {
            int deletedArrows = getWritableDbTask().deleteTask(ids[0]);
            close();
            return deletedArrows;
        }
    }

    public static class UpdateTask extends BaseAsyncTask<Task, Void, Integer> {
        public UpdateTask(Context context, OnFinishListener<Integer> listener) {
            super(context, listener);
        }

        @Override
        protected Integer doInBackground(Task... tasks) {
            int i = getWritableDbTask().updateTask(tasks[0]);
            close();
            return i;
        }

    }

    public static class InsertImportantTask extends BaseAsyncTask<Long, Void, Long> {
        public InsertImportantTask(Context context) {
            super(context);
        }

        public InsertImportantTask(Context context, OnFinishListener<Long> listener) {
            super(context, listener);
        }

        @Override
        protected Long doInBackground(Long... longs) {
            long l = getWritableDbTask().insertImportantTask(longs[0]);
            close();
            return l;
        }

    }

    public static class DeleteImportantTask extends BaseAsyncTask<Long, Void, Integer> {
        public DeleteImportantTask(Context context) {
            super(context);
        }

        public DeleteImportantTask(Context context, OnFinishListener<Integer> listener) {
            super(context, listener);
        }

        @Override
        protected Integer doInBackground(Long... longs) {
            int i = getWritableDbTask().deleteImportantTask(longs[0]);
            close();
            return i;
        }
    }

    public static class IsTaskImportant extends BaseAsyncTask<Long, Void, Boolean> {
        public IsTaskImportant(Context context, OnFinishListener<Boolean> listener) {
            super(context, listener);
        }

        @Override
        protected Boolean doInBackground(Long... longs) {
            boolean taskImportant = getReadableDbTask().isTaskImportant(longs[0]);
            close();
            return taskImportant;
        }
    }

    public static class GetAllImportantTaskList extends BaseAsyncTask<Void, Void, List<Task>> {
        public GetAllImportantTaskList(Context context, OnFinishListener<List<Task>> listener) {
            super(context, listener);
        }

        @Override
        protected List<Task> doInBackground(Void... voids) {
            List<Task> allImportantTaskList = getReadableDbTask().getAllImportantTaskList();
            close();
            return allImportantTaskList;
        }
    }

}
