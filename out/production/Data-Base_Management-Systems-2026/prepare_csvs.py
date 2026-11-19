from pathlib import Path
import csv
import sys

# Run only to rebuild the ready-to-load CSVs from the eight original files.
source = Path(sys.argv[1])
dest = Path(sys.argv[2]) if len(sys.argv) > 2 else Path("data")
dest.mkdir(parents=True, exist_ok=True)

def rows(name):
    with (source / (name + ".csv")).open(encoding="utf-8-sig", newline="") as f:
        yield from csv.DictReader(f)

def write(name, headers, records):
    count = 0
    with (dest / (name + ".csv")).open("w", encoding="utf-8", newline="") as f:
        out = csv.writer(f, lineterminator="\n")
        out.writerow(headers.split())
        for record in records:
            out.writerow(record)
            count += 1
    print(f"{name}: {count:,} rows")

authors = {r["Name"]: r["Author_ID"] for r in rows("Author")}
write("author", "author_id author_name", ((v, k) for k, v in authors.items()))
write("publisher", "publisher_id publisher_name",
      ((r["publisher_id"], r["publisher"]) for r in rows("Publisher")))
write("tag", "tag_id tag_name", ((r["tag_id"], r["tag_name"]) for r in rows("Tag")))
write("user", "user_id user_name", ((r["user_id"], r["username"]) for r in rows("User")))

books = list(rows("Book"))
book_ids = {r["book_id"]: r["id"] for r in books}
write("book", "book_id title publication_year language_code publisher_id",
      ((b["id"], b["title"], b["original_publication_year"], b["language_code"],
        b["publisher_id"]) for b in books))
write("book_author", "book_id author_id",
      ((b["id"], authors[name]) for b in books
       for name in dict.fromkeys(n.strip() for n in b["authors"].split(","))))

def ratings():
    seen = set()
    for r in rows("Rating"):
        pair = (int(r["user_id"]), int(r["book_id"]))
        if pair not in seen:
            seen.add(pair)
            yield (*pair, int(r["rating"]))

def book_tags():
    seen = set()
    for r in rows("Book_Tag"):
        count = int(r["count"])
        pair = (int(book_ids[r["goodreads_book_id"]]), int(r["tag_id"]))
        if count > 0 and pair not in seen:
            seen.add(pair)
            yield (*pair, count)

write("rating", "user_id book_id rating", ratings())
write("book_tag", "book_id tag_id tag_count", book_tags())
write("reading_list", "user_id book_id date_added",
      ((r["user_id"], r["book_id"], r["date_added"]) for r in rows("Reading_List")))
