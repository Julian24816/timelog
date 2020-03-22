CREATE TABLE IF NOT EXISTS log
(
    id integer primary key autoincrement,
    activity text not null,
    start integer not null,
    end integer
);