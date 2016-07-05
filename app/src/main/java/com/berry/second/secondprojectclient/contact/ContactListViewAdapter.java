package com.berry.second.secondprojectclient.contact;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.berry.second.secondprojectclient.R;

import java.util.List;

///**
// * {@link RecyclerView.Adapter} that can display a {@link Contact} and makes a call to the
// * specified {@link OnListFragmentInteractionListener}.
// * TODO: Replace the implementation with code for your data type.
// */
public class ContactListViewAdapter extends RecyclerView.Adapter<ContactListViewAdapter.ViewHolder> {

    private Context mContext;
    private final List<ContactHelper.Contact> mValues;

    public ContactListViewAdapter(Context context, List<ContactHelper.Contact> items) {
        mContext = context;
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_person, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ContactHelper.Contact value=mValues.get(position);

        holder.mItem = value;

        holder.mIdView.setText(value.id.toString());
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.LOLLIPOP)
            holder.mThumbnailView.setImageDrawable(mContext.getDrawable(R.drawable.facebook_no_profile_pic));
        else
            holder.mThumbnailView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.facebook_no_profile_pic));
        holder.mNameView.setText(value.getName());
        holder.mEmailView.setText(value.getEmail());
        holder.mPhoneView.setText(value.getPhone());

//        holder.mView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (null != mListener) {
//                    // Notify the active callbacks interface (the activity, if the
//                    // fragment is attached to one) that an item has been selected.
//                    mListener.onListFragmentInteraction(holder.mItem);
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;

        public final ImageView mThumbnailView;
        public final TextView mIdView;
        public final TextView mNameView;
        public final TextView mEmailView;
        public final TextView mPhoneView;

        public ContactHelper.Contact mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = (TextView) view.findViewById(R.id.id);
            mThumbnailView = (ImageView) view.findViewById(R.id.thumbnailImageView);
            mNameView = (TextView) view.findViewById(R.id.nameTextView);
            mEmailView = (TextView) view.findViewById(R.id.emailTextView);
            mPhoneView = (TextView) view.findViewById(R.id.phoneTextView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mIdView.getText() + " '"
                                            + mNameView.getText() + " '"
                                            + mEmailView.getText() + " '"
                                            + mPhoneView.getText() + " '"
                                            + mPhoneView.getText();
        }
    }
}
