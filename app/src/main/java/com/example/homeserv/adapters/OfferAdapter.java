package com.example.homeserv.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.homeserv.R;
import com.example.homeserv.data.Offer;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OfferAdapter extends RecyclerView.Adapter<OfferAdapter.OfferViewHolder> {

    public interface OnBookClick {
        void onClick(Offer offer);
    }

    private List<Offer> offers;
    private final boolean canBook;
    private final OnBookClick onBookClick;

    public OfferAdapter(List<Offer> offers, boolean canBook, OnBookClick onBookClick) {
        this.offers = offers != null ? offers : new ArrayList<>();
        this.canBook = canBook;
        this.onBookClick = onBookClick;
    }

    public void updateData(List<Offer> newOffers) {
        this.offers = newOffers != null ? newOffers : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public OfferViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_offer, parent, false);
        return new OfferViewHolder(view);
    }

    @Override
    public void onBindViewHolder(OfferViewHolder holder, int position) {
        holder.bind(offers.get(position));
    }

    @Override
    public int getItemCount() {
        return offers.size();
    }

    class OfferViewHolder extends RecyclerView.ViewHolder {
        final TextView serviceBadge, title, description, provider, price, duration;
        final MaterialButton bookButton;

        OfferViewHolder(View itemView) {
            super(itemView);
            serviceBadge = itemView.findViewById(R.id.tvServiceBadge);
            title = itemView.findViewById(R.id.tvOfferTitle);
            description = itemView.findViewById(R.id.tvOfferDescription);
            provider = itemView.findViewById(R.id.tvProviderName);
            price = itemView.findViewById(R.id.tvPrice);
            duration = itemView.findViewById(R.id.tvDuration);
            bookButton = itemView.findViewById(R.id.btnBookNow);
        }

        void bind(Offer offer) {
            serviceBadge.setText(offer.serviceType);
            title.setText(offer.title);
            description.setText(offer.description);
            provider.setText(itemView.getContext().getString(R.string.provider_format, offer.providerName));
            price.setText(itemView.getContext().getString(R.string.price_format, offer.price));
            duration.setText(itemView.getContext().getString(R.string.duration_format, offer.duration));
            bookButton.setVisibility(canBook ? View.VISIBLE : View.GONE);
            bookButton.setOnClickListener(v -> onBookClick.onClick(offer));
        }
    }
}
