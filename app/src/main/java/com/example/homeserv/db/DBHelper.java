package com.example.homeserv.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.homeserv.data.BookingItem;
import com.example.homeserv.data.BookingStatus;
import com.example.homeserv.data.DashboardCounts;
import com.example.homeserv.data.Offer;
import com.example.homeserv.data.Provider;
import com.example.homeserv.data.Roles;
import com.example.homeserv.data.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "homeserv.db";
    private static final int DB_VERSION = 4;
    private static final String DEFAULT_PROVIDER_SERVICE = "General Service";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT NOT NULL UNIQUE," +
                "password TEXT NOT NULL," +
                "role TEXT NOT NULL," +
                "is_blocked INTEGER NOT NULL DEFAULT 0)");

        db.execSQL("CREATE TABLE providers(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "phone TEXT NOT NULL," +
                "service_type TEXT NOT NULL," +
                "user_id INTEGER," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE offers(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "provider_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT NOT NULL," +
                "price REAL NOT NULL," +
                "duration TEXT NOT NULL," +
                "created_at TEXT NOT NULL," +
                "FOREIGN KEY(provider_id) REFERENCES providers(id))");

        db.execSQL("CREATE TABLE bookings(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "offer_id INTEGER NOT NULL," +
                "customer_id INTEGER NOT NULL," +
                "notes TEXT," +
                "date_time TEXT NOT NULL," +
                "status TEXT NOT NULL," +
                "FOREIGN KEY(offer_id) REFERENCES offers(id)," +
                "FOREIGN KEY(customer_id) REFERENCES users(id))");

        seed(db);
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            syncServiceProviderUsers(db);
            ensureDefaultOffersForProviders(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS bookings");
        db.execSQL("DROP TABLE IF EXISTS offers");
        db.execSQL("DROP TABLE IF EXISTS providers");
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    private void seed(SQLiteDatabase db) {
        // Sirf Admin account — baaki sab real users add karenge
        insertUser(db, "Admin", "0000", "admin123", Roles.ADMIN);
    }

    public long registerUser(String name, String phone, String password, String role, String serviceType) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long userId = insertUser(db, name, phone, password, role);
            if (userId > 0 && Roles.PROVIDER.equals(role)) {
                String providerServiceType = (serviceType != null) ? serviceType : DEFAULT_PROVIDER_SERVICE;
                insertProvider(db, name, phone, providerServiceType, (int) userId);
            }
            db.setTransactionSuccessful();
            return userId;
        } finally {
            db.endTransaction();
        }
    }

    private long insertUser(SQLiteDatabase db, String name, String phone, String password, String role) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("password", password);
        values.put("role", role);
        values.put("is_blocked", 0);
        return db.insert("users", null, values);
    }

    public User loginUser(String phone, String password) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,role,is_blocked FROM users WHERE phone=? AND password=? LIMIT 1",
                new String[]{phone, password});
        try {
            if (c.moveToFirst()) {
                User user = cursorToUser(c);
                if (user.isBlocked) return null;
                return user;
            }
            return null;
        } finally {
            c.close();
        }
    }

    public User getUserById(int userId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,role,is_blocked FROM users WHERE id=?",
                new String[]{String.valueOf(userId)});
        try {
            return c.moveToFirst() ? cursorToUser(c) : null;
        } finally {
            c.close();
        }
    }

    public List<User> getCustomers() {
        List<User> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,role,is_blocked FROM users WHERE role=? ORDER BY name",
                new String[]{Roles.CUSTOMER});
        try {
            while (c.moveToNext()) list.add(cursorToUser(c));
        } finally {
            c.close();
        }
        return list;
    }

    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,role,is_blocked FROM users WHERE role != ? ORDER BY role, name",
                new String[]{Roles.ADMIN});
        try {
            while (c.moveToNext()) list.add(cursorToUser(c));
        } finally {
            c.close();
        }
        return list;
    }

    public int setUserBlocked(int userId, boolean blocked) {
        ContentValues values = new ContentValues();
        values.put("is_blocked", blocked ? 1 : 0);
        return getWritableDatabase().update("users", values, "id=?", new String[]{String.valueOf(userId)});
    }

    public long addProvider(String name, String phone, String serviceType, String password) {
        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();
        try {
            long userId = insertUser(db, name, phone, password, Roles.PROVIDER);
            if (userId > 0) {
                insertProvider(db, name, phone, serviceType, (int) userId);
            }
            db.setTransactionSuccessful();
            return userId;
        } finally {
            db.endTransaction();
        }
    }

    private long insertProvider(SQLiteDatabase db, String name, String phone, String serviceType, Integer userId) {
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("service_type", serviceType);
        if (userId == null) values.putNull("user_id");
        else values.put("user_id", userId);
        return db.insert("providers", null, values);
    }

    private void syncServiceProviderUsers(SQLiteDatabase db) {
        Cursor c = db.rawQuery(
                "SELECT u.id,u.name,u.phone FROM users u " +
                "LEFT JOIN providers p ON p.user_id = u.id " +
                "WHERE u.role = ? AND p.id IS NULL",
                new String[]{Roles.PROVIDER});
        try {
            while (c.moveToNext()) {
                long providerId = insertProvider(db,
                        c.getString(c.getColumnIndexOrThrow("name")),
                        c.getString(c.getColumnIndexOrThrow("phone")),
                        DEFAULT_PROVIDER_SERVICE,
                        c.getInt(c.getColumnIndexOrThrow("id")));

            }
        } finally {
            c.close();
        }
    }

    public List<Provider> getProviders() {
        List<Provider> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,service_type,user_id FROM providers ORDER BY name", null);
        try {
            while (c.moveToNext()) list.add(cursorToProvider(c));
        } finally {
            c.close();
        }
        return list;
    }

    public List<Provider> getProvidersForUser(int userId, String role) {
        if (Roles.ADMIN.equals(role)) return getProviders();
        List<Provider> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT id,name,phone,service_type,user_id FROM providers WHERE user_id=? ORDER BY name",
                new String[]{String.valueOf(userId)});
        try {
            while (c.moveToNext()) list.add(cursorToProvider(c));
        } finally {
            c.close();
        }
        return list;
    }

    public long addOffer(int providerId, String title, String description, double price, String duration) {
        return insertOffer(getWritableDatabase(), providerId, title, description, price, duration);
    }

    private long insertOffer(SQLiteDatabase db, int providerId, String title, String description, double price, String duration) {
        ContentValues values = new ContentValues();
        values.put("provider_id", providerId);
        values.put("title", title);
        values.put("description", description);
        values.put("price", price);
        values.put("duration", duration);
        values.put("created_at", now());
        return db.insert("offers", null, values);
    }

    private long insertDefaultOfferForProvider(SQLiteDatabase db, int providerId, String serviceType) {
        String cleanType = (serviceType == null || serviceType.isBlank()) ? DEFAULT_PROVIDER_SERVICE : serviceType;
        return insertOffer(db, providerId,
                cleanType + " Service",
                "Professional " + cleanType + " service provider is available for booking. Contact provider for complete details.",
                0.0, "Contact provider");
    }

    private void ensureDefaultOffersForProviders(SQLiteDatabase db) {
        // Default offers disabled - providers post their own offers via PostOfferActivity
    }

    public List<Offer> getOffers() {
        List<Offer> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT o.id,o.provider_id,p.name AS provider_name,p.service_type,o.title,o.description,o.price,o.duration " +
                "FROM offers o INNER JOIN providers p ON p.id=o.provider_id WHERE o.price > 0 ORDER BY o.id DESC", null);
        try {
            while (c.moveToNext()) list.add(cursorToOffer(c));
        } finally {
            c.close();
        }
        return list;
    }

    public Offer getOfferById(int offerId) {
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT o.id,o.provider_id,p.name AS provider_name,p.service_type,o.title,o.description,o.price,o.duration " +
                "FROM offers o INNER JOIN providers p ON p.id=o.provider_id WHERE o.id=?",
                new String[]{String.valueOf(offerId)});
        try {
            return c.moveToFirst() ? cursorToOffer(c) : null;
        } finally {
            c.close();
        }
    }

    public long addBooking(int offerId, int customerId, String notes) {
        return insertBooking(getWritableDatabase(), offerId, customerId, notes);
    }

    private long insertBooking(SQLiteDatabase db, int offerId, int customerId, String notes) {
        ContentValues values = new ContentValues();
        values.put("offer_id", offerId);
        values.put("customer_id", customerId);
        values.put("notes", notes);
        values.put("date_time", now());
        values.put("status", BookingStatus.ACTIVE);
        return db.insert("bookings", null, values);
    }

    public List<BookingItem> getBookingsForUser(User user) {
        if (Roles.ADMIN.equals(user.role))
            return queryBookings("ORDER BY b.id DESC", new String[]{});
        else if (Roles.PROVIDER.equals(user.role))
            return queryBookings("WHERE p.user_id=? ORDER BY b.id DESC", new String[]{String.valueOf(user.id)});
        else
            return queryBookings("WHERE b.customer_id=? ORDER BY b.id DESC", new String[]{String.valueOf(user.id)});
    }

    private List<BookingItem> queryBookings(String whereClause, String[] args) {
        List<BookingItem> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery(
                "SELECT b.id,b.offer_id,o.title AS service_title,u.name AS customer_name,p.name AS provider_name," +
                "o.price,b.date_time,b.status,IFNULL(b.notes,'') AS notes " +
                "FROM bookings b " +
                "INNER JOIN offers o ON o.id=b.offer_id " +
                "INNER JOIN providers p ON p.id=o.provider_id " +
                "INNER JOIN users u ON u.id=b.customer_id " + whereClause, args);
        try {
            while (c.moveToNext()) {
                list.add(new BookingItem(
                        c.getInt(c.getColumnIndexOrThrow("id")),
                        c.getInt(c.getColumnIndexOrThrow("offer_id")),
                        c.getString(c.getColumnIndexOrThrow("service_title")),
                        c.getString(c.getColumnIndexOrThrow("customer_name")),
                        c.getString(c.getColumnIndexOrThrow("provider_name")),
                        c.getDouble(c.getColumnIndexOrThrow("price")),
                        c.getString(c.getColumnIndexOrThrow("date_time")),
                        c.getString(c.getColumnIndexOrThrow("status")),
                        c.getString(c.getColumnIndexOrThrow("notes"))
                ));
            }
        } finally {
            c.close();
        }
        return list;
    }

    public int markBookingCompleted(int bookingId) {
        ContentValues values = new ContentValues();
        values.put("status", BookingStatus.COMPLETED);
        return getWritableDatabase().update("bookings", values, "id=?", new String[]{String.valueOf(bookingId)});
    }

    public DashboardCounts getDashboardCounts() {
        return new DashboardCounts(countRealOffers(), countProviders(), count("bookings"));
    }

    private int count(String table) {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM " + table, null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private int countRealOffers() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM offers WHERE price > 0", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private int countProviders() {
        Cursor c = getReadableDatabase().rawQuery("SELECT COUNT(*) FROM providers", null);
        try {
            return c.moveToFirst() ? c.getInt(0) : 0;
        } finally {
            c.close();
        }
    }

    private String now() {
        return new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(new Date());
    }

    private User cursorToUser(Cursor c) {
        return new User(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getString(c.getColumnIndexOrThrow("role")),
                c.getInt(c.getColumnIndexOrThrow("is_blocked")) == 1
        );
    }

    private Provider cursorToProvider(Cursor c) {
        int userIdIdx = c.getColumnIndexOrThrow("user_id");
        Integer userId = c.isNull(userIdIdx) ? null : c.getInt(userIdIdx);
        return new Provider(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getString(c.getColumnIndexOrThrow("name")),
                c.getString(c.getColumnIndexOrThrow("phone")),
                c.getString(c.getColumnIndexOrThrow("service_type")),
                userId
        );
    }

    private Offer cursorToOffer(Cursor c) {
        return new Offer(
                c.getInt(c.getColumnIndexOrThrow("id")),
                c.getInt(c.getColumnIndexOrThrow("provider_id")),
                c.getString(c.getColumnIndexOrThrow("provider_name")),
                c.getString(c.getColumnIndexOrThrow("service_type")),
                c.getString(c.getColumnIndexOrThrow("title")),
                c.getString(c.getColumnIndexOrThrow("description")),
                c.getDouble(c.getColumnIndexOrThrow("price")),
                c.getString(c.getColumnIndexOrThrow("duration"))
        );
    }
}
