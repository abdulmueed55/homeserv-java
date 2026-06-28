package com.example.homeserv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserv.R;
import com.example.homeserv.data.BookingItem;
import com.example.homeserv.data.BookingStatus;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {

    public interface OnCompleteClick {
        void onClick(BookingItem booking);
    }

    private List<BookingItem> bookings;
    private final boolean canComplete;
    private final OnCompleteClick onCompleteClick;

    public BookingAdapter(List<BookingItem> bookings, boolean canComplete, OnCompleteClick onCompleteClick) {
        this.bookings = bookings != null ? bookings : new ArrayList<>();
        this.canComplete = canComplete;
        this.onCompleteClick = onCompleteClick;
    }

    public void updateData(List<BookingItem> newBookings) {
        this.bookings = newBookings != null ? newBookings : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public BookingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BookingViewHolder holder, int position) {
        holder.bind(bookings.get(position));
    }

    @Override
    public int getItemCount() {
        return bookings.size();
    }

    class BookingViewHolder extends RecyclerView.ViewHolder {
        final TextView bookingId, service, customer, price, dateTime, status;
        final MaterialButton completeButton;

        BookingViewHolder(View itemView) {
            super(itemView);
            bookingId = itemView.findViewById(R.id.tvBookingId);
            service = itemView.findViewById(R.id.tvBookingService);
            customer = itemView.findViewById(R.id.tvBookingCustomer);
            price = itemView.findViewById(R.id.tvBookingPrice);
            dateTime = itemView.findViewById(R.id.tvBookingDateTime);
            status = itemView.findViewById(R.id.tvBookingStatus);
            completeButton = itemView.findViewById(R.id.btnMarkCompleted);
        }

        void bind(BookingItem booking) {
            bookingId.setText(itemView.getContext().getString(R.string.booking_id_format, booking.id));
            service.setText(booking.serviceTitle);
            customer.setText(itemView.getContext().getString(R.string.customer_format, booking.customerName));
            price.setText(itemView.getContext().getString(R.string.price_format, booking.price));
            dateTime.setText(booking.dateTime);
            status.setText(booking.status);
            int colorRes = BookingStatus.COMPLETED.equals(booking.status) ? R.color.status_completed : R.color.status_active;
            status.setBackgroundTintList(ContextCompat.getColorStateList(itemView.getContext(), colorRes));
            boolean showComplete = canComplete && BookingStatus.ACTIVE.equals(booking.status);
            completeButton.setVisibility(showComplete ? View.VISIBLE : View.GONE);
            completeButton.setOnClickListener(v -> onCompleteClick.onClick(booking));
        }
    }
}
