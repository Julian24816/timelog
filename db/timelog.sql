CREATE TABLE IF NOT EXISTS log
(
    id       integer not null primary key autoincrement,
    activity integer default 0 not null references activity on update cascade on delete restrict,
    what     text    not null,
    start    integer not null,
    end      integer
);

CREATE TABLE IF NOT EXISTS activity
(
    id     integer not null primary key autoincrement,
    parent integer default 0 not null references activity on update cascade on delete set default,
    name   text not null
);
