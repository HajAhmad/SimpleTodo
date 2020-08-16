package ir.simpletodo;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import ir.simpletodo.data.DbAsyncOps;
import ir.simpletodo.task.Task;
import ir.simpletodo.task.TaskDetailActivity;

import static ir.simpletodo.AppConstants.ACTION_DELETE_CODE;
import static ir.simpletodo.AppConstants.ACTION_INSERT_CODE;
import static ir.simpletodo.AppConstants.ACTION_UPDATE_CODE;
import static ir.simpletodo.AppConstants.DISPLAY_ACTIVES;
import static ir.simpletodo.AppConstants.DISPLAY_ALL;
import static ir.simpletodo.AppConstants.DISPLAY_DONE;
import static ir.simpletodo.AppConstants.DISPLAY_IMPORTANTS;
import static ir.simpletodo.AppConstants.STATUS_ACTIVE;
import static ir.simpletodo.AppConstants.STATUS_DONE;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mMainList;
    private TaskListAdapter mAdapter;
    private FloatingActionButton mFabAdd;
    private static int listDisplayStatus = DISPLAY_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        mMainList = findViewById(R.id.activity_main_list);
        mFabAdd = findViewById(R.id.fab);
        mFabAdd.setOnClickListener(view -> openNewTaskPage());

        ImageView filterAction = toolbar.findViewById(R.id.activity_main_toolbar_filter_action);

        filterAction.setOnClickListener(v -> {
            PopupMenu menu = new PopupMenu(this, v);
            menu.getMenuInflater().inflate(R.menu.filter_menu, menu.getMenu());
            menu.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.item_important:
                        showImportantTaskList();
                        break;
                    case R.id.item_active:
                        showActiveTaskList();
                        break;
                    case R.id.item_done:
                        showDoneTaskList();
                        break;
                    case R.id.item_all:
                        showAllTaskList();
                        break;
                }
                return false;
            });
            menu.show();
        });
        filterAction.setOnLongClickListener(v -> {
            Toast.makeText(this, R.string.action_filter_title, Toast.LENGTH_SHORT).show();
            return true;
        });

        maintainViews();

    }

    private void maintainViews() {
        new DbAsyncOps.GetTasksCount(this, count -> {
            if (count > 0) {
                showMainList();
                showAllTaskList();
            } else {
                showEmptyListMessage();
            }
        }).execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultActionCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultActionCode, data);
        if (resultActionCode != RESULT_CANCELED) {
            Objects.requireNonNull(data);
            long taskId = data.getLongExtra(TaskDetailActivity._key_task_id,
                    TaskDetailActivity._key_def_val);
            switch (resultActionCode) {
                case ACTION_INSERT_CODE:
                    if (mAdapter == null) showMainList();
                    insertNewTaskInList(taskId);
                    break;
                case ACTION_UPDATE_CODE:
                    updateTaskInList(taskId);
                    break;
                case ACTION_DELETE_CODE:
                    removeItemFromList(taskId);
                    break;
                default:
                    throw new IllegalArgumentException("Action code is not valid: " +
                            resultActionCode);
            }
        }
    }

    private void isTaskImportant(long taskId) {
        new DbAsyncOps.IsTaskImportant(this, isImportant -> {
            if (isImportant)
                mAdapter.changeItemImportance(mAdapter.getItemPositionById(taskId),
                        true);
        }).execute(taskId);
    }

    private void removeItemFromList(long taskId) {
        new DbAsyncOps.GetTask(this, task -> {
            new DbAsyncOps.DeleteTask(this, effectedRow -> {
                if (isItemForCurrentList(task))
                    mAdapter.removeItemsById(taskId);
            }).execute(taskId);
        }).execute(taskId);
    }

    private void showEmptyListMessage() {
        mMainList.setVisibility(View.GONE);
        findViewById(R.id.activity_main_empty_list_message).setVisibility(View.VISIBLE);
        findViewById(R.id.activity_main_empty_list_message)
                .setOnClickListener(v -> openNewTaskPage());
    }

    private void showMainList() {
        findViewById(R.id.activity_main_empty_list_message).setVisibility(View.GONE);
        mMainList.setVisibility(View.VISIBLE);
        mMainList.setItemAnimator(new DefaultItemAnimator());
        mMainList.setLayoutManager(new LinearLayoutManager(this));
        mMainList.addItemDecoration(new DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        mAdapter = new TaskListAdapter(new TaskListAdapter.OnItemActionListener() {
            @Override
            public void onItemClicked(long taskId, int position) {
                openTaskDetailPage(taskId);
            }

            @Override
            public void onCheckChanged(Task task, boolean isChecked, int position) {
                new DbAsyncOps.UpdateTask(MainActivity.this, effectedRows -> {
                    mMainList.post(() -> {
                        mAdapter.setItemAt(position, task);
                        if (listDisplayStatus == DISPLAY_ALL)
                            mAdapter.notifyItemChanged(position);
                        else if ((listDisplayStatus == DISPLAY_DONE && !isChecked) ||
                                (listDisplayStatus == DISPLAY_ACTIVES && isChecked))
                            mAdapter.removeItemAt(position);
                        else
                            throw new IllegalArgumentException("\"listDisplayStatus\" is undefined: " +
                                    listDisplayStatus);
                    });
                }).execute(task);
            }

            @Override
            public void onImportantActionClicked(long taskId, boolean isImportant,
                    int itemPosition) {
                if (isImportant) {
                    new DbAsyncOps.InsertImportantTask(MainActivity.this, insertedId -> {
                        mAdapter.changeItemImportance(itemPosition, true);
                    }).execute(taskId);
                } else {
                    new DbAsyncOps.DeleteImportantTask(MainActivity.this, effectedRows -> {
                        if (listDisplayStatus == DISPLAY_ALL)
                            mAdapter.changeItemImportance(itemPosition, false);
                        else if (listDisplayStatus == DISPLAY_IMPORTANTS)
                            mAdapter.removeItemAt(itemPosition);
                    }).execute(taskId);
                }
            }
        });
        mMainList.setAdapter(mAdapter);
        mMainList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) mFabAdd.hide();
                else if (dy < 0) mFabAdd.show();
            }
        });
    }


    private void showAllTaskList() {
        new DbAsyncOps.GetAllTaskList(this, taskList -> {
            if (taskList != null && taskList.size() > 0) mAdapter.setItemList(taskList);
            listDisplayStatus = DISPLAY_ALL;
        }).execute();
    }

    private void insertNewTaskInList(long newlyInsertedTaskId) {
        new DbAsyncOps.GetTask(this, insertedTask -> {
            if (isItemForCurrentList(insertedTask)) {
                mAdapter.addItemAt(insertedTask, 0);
                mMainList.post(() -> mMainList.scrollToPosition(0));
                isTaskImportant(insertedTask.getId());
            }
        }).execute(newlyInsertedTaskId);
    }

    private void updateTaskInList(long updatedTaskId) {
        new DbAsyncOps.GetTask(this, task -> {
            if (isItemForCurrentList(task)) {
                new DbAsyncOps.IsTaskImportant(this, isImportant -> {
                    task.setImportant(isImportant);
                    mAdapter.setItemAt(mAdapter.getItemPositionById(updatedTaskId), task);
                }).execute(updatedTaskId);
            }
        }).execute(updatedTaskId);
    }

    private boolean isItemForCurrentList(Task task) {
        if (listDisplayStatus == DISPLAY_ALL ||
                (listDisplayStatus == DISPLAY_IMPORTANTS && task.isImportant()) ||
                (listDisplayStatus == DISPLAY_DONE && task.getStatus() == STATUS_DONE) ||
                (listDisplayStatus == DISPLAY_ACTIVES && task.getStatus() == STATUS_ACTIVE)
        ) {
            return true;
        } else {
            removeItemFromListIfExists(task);
            return false;
        }
    }

    private void removeItemFromListIfExists(Task task) {
        int itemPositionInList = mAdapter.getItemPositionById(task.getId());
        if (itemPositionInList >= 0)
            mAdapter.removeItemAt(itemPositionInList);
    }

    private void openNewTaskPage() {
        TaskDetailActivity.startNewTaskActivity(this);
    }

    private void openTaskDetailPage(long taskId) {
        TaskDetailActivity.startTaskDetailActivity(this, taskId);
    }

    private void showImportantTaskList() {
        new DbAsyncOps.GetAllImportantTaskList(this, taskList -> {
            if (taskList != null && taskList.size() > 0) {
                mAdapter.setItemList(taskList);
                listDisplayStatus = DISPLAY_IMPORTANTS;
            } else {
                Toast.makeText(this, R.string.no_important_task_message,
                        Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void showDoneTaskList() {
        new DbAsyncOps.GetAllDoneTaskList(this, taskList -> {
            if (taskList != null && taskList.size() > 0) {
                mAdapter.setItemList(taskList);
                listDisplayStatus = DISPLAY_DONE;
            } else {
                Toast.makeText(this, R.string.no_done_task_message, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

    private void showActiveTaskList() {
        new DbAsyncOps.GetAllActiveTaskList(this, list -> {
            if (list != null && list.size() > 0) {
                mAdapter.setItemList(list);
                listDisplayStatus = DISPLAY_ACTIVES;
            } else {
                Toast.makeText(this, R.string.no_active_task_message, Toast.LENGTH_SHORT).show();
            }
        }).execute();
    }

}