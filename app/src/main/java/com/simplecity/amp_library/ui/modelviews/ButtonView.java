package com.simplecity.amp_library.ui.modelviews;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.simplecity.amp_library.R;
import com.simplecityapps.recycler_adapter.model.BaseViewModel;
import com.simplecityapps.recycler_adapter.recyclerview.BaseViewHolder;

import static com.simplecity.amp_library.R.layout.list_item_button;
import static com.simplecity.amp_library.ui.adapters.ViewType.BUTTON;

/**
 * Created by peyman on 4/8/18.
 */
public class ButtonView extends BaseViewModel<ButtonView.ViewHolder> {

    public interface OnClickListener {
        void onButtonClick(int position, ButtonView buttonView, ButtonView.ViewHolder viewHolder);
    }

    public String buttonText;

    public ButtonView(String text) {
        this.buttonText = text;
    }

    @Nullable
    private ButtonView.OnClickListener listener;

    public void setListener(@Nullable ButtonView.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getViewType() {
        return BUTTON;
    }

    @Override
    public ViewHolder createViewHolder(ViewGroup parent) {
        return new ViewHolder(createView(parent));
    }

    @Override
    public int getLayoutResId() {
        return list_item_button;
    }

    @Override
    public void bindView(ButtonView.ViewHolder holder) {
        super.bindView(holder);

        holder.textView.setText(buttonText);
    }

    void onButtonClicked(int position, ButtonView.ViewHolder viewHolder) {
        if (listener != null) {
            listener.onButtonClick(position, this, viewHolder);
        }
    }


    public static class ViewHolder extends BaseViewHolder<ButtonView> {
        public TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.text);
            itemView.setOnClickListener(v -> viewModel.onButtonClicked(getAdapterPosition(), this));
        }

        @Override
        public String toString() {
            return "ButtonView.ViewHolder";
        }
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean areContentsEqual(Object other) {
        return equals(other);
    }
}
