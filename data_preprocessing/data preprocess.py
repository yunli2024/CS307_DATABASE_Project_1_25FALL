import csv
import re

def parse_iso8601_duration_to_seconds(s: str) -> int:
    if not s:
        raise ValueError("empty duration")

    s = s.strip()
    if not s.startswith('P'):
        raise ValueError(f"Not an ISO 8601 duration: {s}")

    s = s[1:]

    weeks = days = hours = minutes = seconds = 0
    num = ''
    for ch in s:
        if ch == 'T':
            continue
        if ch.isdigit():
            num += ch
            continue

        if not num:
            continue

        val = int(num)
        if ch == 'W':
            weeks = val
        elif ch == 'D':
            days = val
        elif ch == 'H':
            hours = val
        elif ch == 'M':
            minutes = val
        elif ch == 'S':
            seconds = val
        num = ''

    total_seconds = (((weeks * 7) + days) * 24 + hours) * 3600 + minutes * 60 + seconds
    return total_seconds


def seconds_to_iso8601(total_seconds: int) -> str:
    if total_seconds < 0:
        raise ValueError("duration cannot be negative")

    days, rem = divmod(total_seconds, 24 * 3600)
    hours, rem = divmod(rem, 3600)
    minutes, seconds = divmod(rem, 60)

    parts = ["P"]
    if days:
        parts.append(f"{days}D")

    parts.append("T")
    if hours:
        parts.append(f"{hours}H")
    if minutes:
        parts.append(f"{minutes}M")
    if seconds or (not hours and not minutes and not days):
        parts.append(f"{seconds}S")

    return "".join(parts)

def clean_reviews(
    input_path: str,
    output_path: str,
    expected_fixed_lines: int = 19
):

    pattern = re.compile(r'^(\d+),,(\d+,)')

    fixed_extra_comma_lines = 0

    with open(input_path, "r", encoding="utf-8", newline="") as fin, \
         open(output_path, "w", encoding="utf-8", newline="") as fout:

        header_line = fin.readline()
        if not header_line:
            print("[clean_reviews] Empty file, nothing to do.")
            return

        header_row = next(csv.reader([header_line]))
        writer = csv.writer(fout)
        writer.writerow(header_row)

        try:
            recipe_idx = header_row.index("RecipeId")
        except ValueError:
            recipe_idx = 1

        for line in fin:
            cleaned_line, n = pattern.subn(r'\1,\2', line, count=1)
            if n > 0:
                fixed_extra_comma_lines += 1

            for row in csv.reader([cleaned_line]):
                if len(row) == 0:
                    continue
                if len(row) <= recipe_idx:
                    writer.writerow(row)
                    continue

                recipe_id = row[recipe_idx].strip()
                if recipe_id.endswith(".0"):
                    try:
                        row[recipe_idx] = str(int(float(recipe_id)))  # "123.0" -> "123"
                    except ValueError:
                        pass

                writer.writerow(row)

    print(f"[clean_reviews] Fixed {fixed_extra_comma_lines} lines with leading 'ID,,ID,'.")
    if expected_fixed_lines is not None and fixed_extra_comma_lines != expected_fixed_lines:
        print(
            f"  [Warning] Expected to fix ~{expected_fixed_lines} lines, "
            f"but actually fixed {fixed_extra_comma_lines}."
        )


def fix_recipes_time(
    input_path: str,
    output_path: str
):

    with open(input_path, "r", encoding="utf-8", newline="") as fin, \
         open(output_path, "w", encoding="utf-8", newline="") as fout:

        reader = csv.DictReader(fin)
        fieldnames = reader.fieldnames
        if fieldnames is None:
            raise RuntimeError("recipes.csv has no header row")

        writer = csv.DictWriter(fout, fieldnames=fieldnames)
        writer.writeheader()

        fixed_count = 0
        total_count = 0

        for row in reader:
            total_count += 1

            cook = (row.get("CookTime") or "").strip()
            prep = (row.get("PrepTime") or "").strip()
            total_time = (row.get("TotalTime") or "").strip()

            # 只在三个字段都有值时做检查
            if cook and prep and total_time:
                try:
                    cook_s = parse_iso8601_duration_to_seconds(cook)
                    prep_s = parse_iso8601_duration_to_seconds(prep)
                    total_s = parse_iso8601_duration_to_seconds(total_time)
                except ValueError:
                    pass
                else:
                    expected_total_s = cook_s + prep_s
                    if expected_total_s != total_s:
                        row["TotalTime"] = seconds_to_iso8601(expected_total_s)
                        fixed_count += 1

            writer.writerow(row)

    print(f"[fix_recipes_time] Checked {total_count} records, fixed {fixed_count} inconsistent TotalTime values.")



if __name__ == "__main__":
    INPUT_REVIEWS = "data/reviews.csv"
    OUTPUT_REVIEWS = "data/reviews_cleaned.csv"

    INPUT_RECIPES = "data/recipes.csv"
    OUTPUT_RECIPES = "data/recipes_cleaned.csv"
    print("Fix reviews.csv with .0 id and extra comma\n")
    clean_reviews(INPUT_REVIEWS, OUTPUT_REVIEWS, expected_fixed_lines=19)

    print("Fix time in recipes.csv\n")
    fix_recipes_time(INPUT_RECIPES, OUTPUT_RECIPES)

    print("All preprocessing steps finished.")
