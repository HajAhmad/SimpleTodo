package ir.simpletodo;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ir.simpletodo.task.Task;

import static ir.simpletodo.AppConstants.STATUS_DONE;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private List<Task> mList;
    private OnItemActionListener mListener;

    public TaskListAdapter(@NonNull OnItemActionListener listener) {
        mListener = Objects.requireNonNull(listener);
        mList = new ArrayList<>();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return mList.get(position).getId();
    }

    @NonNull
    @Override
    public TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TaskListViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_main_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        Task item = getItemAt(position);
        holder.titleText.setText(item.getTitle());
        if (!TextUtils.isEmpty(item.getDescription()))
            holder.descriptionText.setText(item.getDescription());

        holder.checkBox.setChecked(item.getStatus() == AppConstants.STATUS_DONE);
        holder.checkBox.setOnClickListener(buttonView -> {
            boolean isChecked = holder.checkBox.isChecked();
            item.setStatus(isChecked ? STATUS_DONE : AppConstants.STATUS_ACTIVE);
            mListener.onCheckChanged(item, isChecked, position);
        });

        if (item.isImportant())
            holder.importantMark.setImageDrawable(ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.ic_baseline_star_24));
        else
            holder.importantMark.setImageDrawable(ContextCompat.getDrawable(
                    holder.itemView.getContext(), R.drawable.ic_baseline_star_border_24));

        holder.importantMark.setOnClickListener(v ->
                mListener.onImportantActionClicked(item.getId(), !item.isImportant(), position));

        holder.container.setOnClickListener(v ->
                mListener.onItemClicked(item.getId(), position));
    }

    @Override
    public int getItemCount() {
        return mList != null ? mList.size() : 0;
    }

    Task getItemAt(int index) {
        return mList.get(index);
    }

    void setItemList(List<Task> taskList) {
        if (mList.size() > 0) clear();

        mList = new ArrayList<>(taskList);
        notifyItemRangeInserted(0, mList.size());
    }

    void addItem(Task task) {
        addItemAt(task, -1);
    }

    void addItemAt(Task task, int position) {
        if (position == -1) {
            mList.add(task);
            notifyItemInserted(getItemCount() - 1);
        } else if (position > -1) {
            mList.add(position, task);
            notifyItemInserted(position);
        }
    }

    public void removeItemAt(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    private void clear() {
        int currentSize = getItemCount();
        mList = new ArrayList<>();
        notifyItemRangeRemoved(0, currentSize);
    }

    int getItemPositionById(long id) {
        for (int i = 0; i < mList.size(); i++)
            if (id == mList.get(i).getId())
                return i;

        return -1;
    }

    void setItemAt(int position, Task newTask) {
        mList.set(position, newTask);
        notifyItemChanged(position);
    }

    void removeItemsById(long id) {
        removeItemAt(getItemPositionById(id));
    }

    void changeItemImportance(int itemPosition, boolean isImportant) {
        getItemAt(itemPosition).setImportant(isImportant);
        notifyItemChanged(itemPosition);
    }

    interface OnItemActionListener {
        void onItemClicked(long taskId, int position);

        void onCheckChanged(Task task, boolean isChecked, int position);

        void onImportantActionClicked(long taskId, boolean isImportant, int itemPosition);
    }

    static class TaskListViewHolder extends RecyclerView.ViewHolder {
        ViewGroup container;
        TextView titleText;
        TextView descriptionText;
        CheckBox checkBox;
        ImageView importantMark;

        TaskListViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.main_list_item_container);
            titleText = itemView.findViewById(R.id.main_list_item_title_text);
            descriptionText = itemView.findViewById(R.id.main_list_item_description_text);
            checkBox = itemView.findViewById(R.id.main_list_item_checkbox);
            importantMark = itemView.findViewById(R.id.main_list_item_important_image);

        }
    }
}
