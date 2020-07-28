package ir.simpletodo.task;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import ir.simpletodo.AppUtil;
import ir.simpletodo.R;
import ir.simpletodo.data.DbAsyncOps;

import static ir.simpletodo.AppConstants.ACTION_DELETE_CODE;
import static ir.simpletodo.AppConstants.ACTION_INSERT_CODE;
import static ir.simpletodo.AppConstants.ACTION_UPDATE_CODE;
import static ir.simpletodo.AppConstants.STATUS_ACTIVE;
import static ir.simpletodo.AppConstants.STATUS_DONE;

public class TaskDetailActivity extends AppCompatActivity {

    public static final String _key_task_id = "_task_id";
    public static final int _key_def_val = -1;
    private long mTaskId = _key_def_val;
    private boolean isImportant;
    private TextView mTitleText;
    private TextView mDescriptionText;
    private CheckBox mStatusCheckBox;

    public static void startNewTaskActivity(Activity activity) {
        startTaskDetailActivity(activity, _key_def_val);
    }

    public static void startTaskDetailActivity(Activity activity, long taskId) {
        Intent starter = new Intent(activity, TaskDetailActivity.class);
        if (taskId != _key_def_val) {
            starter.putExtra(_key_task_id, taskId);
            activity.startActivityForResult(starter, ACTION_UPDATE_CODE);
        } else {
            activity.startActivityForResult(starter, ACTION_INSERT_CODE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mTaskId = getIntent().getLongExtra(_key_task_id, _key_def_val);

        setContentView(R.layout.activity_task_detail);

        Toolbar toolbar = findViewById(R.id.activity_task_detail_toolbar);
        setSupportActionBar(toolbar);

        TextView toolbarTitleText = findViewById(R.id.activity_task_detail_toolbar_title_text);
        mTitleText = findViewById(R.id.activity_task_detail_title_text);
        mDescriptionText = findViewById(R.id.activity_task_detail_description_text);
        mStatusCheckBox = findViewById(R.id.activity_task_detail_status_checkbox);
        ImageView importantAction = findViewById(R.id.activity_task_detail_important_mark_image);
        ImageView returnAction = findViewById(R.id.activity_task_detail_toolbar_return_action);

        ImageView removeAction = findViewById(R.id.activity_task_detail_toolbar_remove_action);

        returnAction.setOnClickListener(v -> saveAndExit());

        if (isUpdateMode()) {
            toolbarTitleText.setText(R.string.edit_task_title);
            new DbAsyncOps.GetTask(this, task -> {
                mTitleText.setText(task.getTitle());
                if (!TextUtils.isEmpty(task.getDescription()))
                    mDescriptionText.setText(task.getDescription());

                mStatusCheckBox.setChecked(task.getStatus() == STATUS_DONE);

                new DbAsyncOps.IsTaskImportant(this, is ->
                        changeImportant(importantAction, is)).execute(mTaskId);

            }).execute(mTaskId);

            removeAction.setVisibility(View.VISIBLE);
            removeAction.setOnClickListener(v -> {
                new DbAsyncOps.DeleteTask(this, effectedRow -> {
                    setResult(ACTION_DELETE_CODE, new Intent() {{
                        putExtra(_key_task_id, mTaskId);
                    }});

                    finish();
                }).execute(mTaskId);
            });

        } else {
            toolbarTitleText.setText(R.string.add_task_title);
        }

        mStatusCheckBox.setOnLongClickListener(v -> {
            if (mStatusCheckBox.isChecked())
                Toast.makeText(this, R.string.mark_as_not_done_title, Toast.LENGTH_SHORT)
                        .show();
            else
                Toast.makeText(this, R.string.mark_as_done_title, Toast.LENGTH_SHORT)
                        .show();
            return true;
        });

        importantAction.setOnClickListener(v -> changeImportant((ImageView) v, !isImportant));
        importantAction.setOnLongClickListener(v -> {
            if (isImportant)
                Toast.makeText(this, R.string.mark_as_not_important_title,
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, R.string.mark_as_important_title,
                        Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) saveAndExit();

        return super.onKeyDown(keyCode, event);
    }


    /**
     * TODO:
     * 1- moshkele ezafe kardane ye Taske jadid, darsoorati ke safhe taske jadid khalie.
     * 2- moshkele namayesh Filter ha
     */
    private void saveAndExit() {
        if (isInfoValid(mTitleText.getText())) {
            if (isUpdateMode()) {
                updateTask(mTaskId, mTitleText.getText().toString(),
                        TextUtils.isEmpty(mDescriptionText.getText()) ?
                                null : mDescriptionText.getText().toString(),
                        mStatusCheckBox.isChecked() ? STATUS_DONE : STATUS_ACTIVE);
            } else {
                insertNewTask(mTitleText.getText().toString(),
                        TextUtils.isEmpty(mDescriptionText.getText()) ?
                                null : mDescriptionText.getText().toString(),
                        mStatusCheckBox.isChecked() ? STATUS_DONE : STATUS_ACTIVE,
                        AppUtil.getCurrentDate());
            }
        } else {
            setResult(RESULT_CANCELED);
        }
    }

    private void changeImportant(ImageView imageView, boolean isTaskImportant) {
        if (isTaskImportant) {
            imageView.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_baseline_star_24));

            if (isUpdateMode())
                new DbAsyncOps.InsertImportantTask(this).execute(mTaskId);
        } else {
            imageView.setImageDrawable(ContextCompat.getDrawable(this,
                    R.drawable.ic_baseline_star_border_24));

            if (isUpdateMode())
                new DbAsyncOps.DeleteImportantTask(this).execute(mTaskId);
        }

        this.isImportant = isTaskImportant;
    }

    private boolean isUpdateMode() {
        return mTaskId != _key_def_val;
    }

    private void updateTask(long id, String title, String description, int status) {
        new DbAsyncOps.UpdateTask(this, effectedRows -> {
            setResult(ACTION_UPDATE_CODE, new Intent() {{
                putExtra(_key_task_id, mTaskId);
            }});
            finish();
        }).execute(new Task(id, title, description, status));
    }

    private void insertNewTask(String title, String description, int status, String creationDate) {
        new DbAsyncOps.InsertTask(this, insertedTaskId -> {
            mTaskId = insertedTaskId;
            if (isImportant) {
                new DbAsyncOps.InsertImportantTask(this, insertedId -> {
                    setResult(ACTION_INSERT_CODE, new Intent() {{
                        putExtra(_key_task_id, mTaskId);
                    }});
                    finish();
                }).execute(mTaskId);
            } else {
                setResult(ACTION_INSERT_CODE, new Intent() {{
                    putExtra(_key_task_id, mTaskId);
                }});
                finish();
            }
        }).execute(new Task() {{
            setTitle(title);
            setDescription(description);
            setStatus(status);
            setCreationDate(creationDate);
        }});
    }

    private boolean isInfoValid(CharSequence title) {
        return title != null && !TextUtils.isEmpty(title);
    }

}
