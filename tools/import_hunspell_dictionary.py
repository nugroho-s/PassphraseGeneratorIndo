#!/usr/bin/env python3
"""Rebuild the bundled dictionary from the vendored, pinned Hunspell source."""
import argparse
import hashlib
from pathlib import Path
import unicodedata

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "third_party/hunspell-id/id_ID.dic"
TARGET = ROOT / "app/src/main/assets/words_list.txt"
SOURCE_SHA256 = "42cf27a0ba5966eaf72846419200a0318ef4db42066947bd840b140604b3385c"
EXPECTED_WORD_COUNT = 42816


def build_dictionary():
    data = SOURCE.read_bytes()
    if hashlib.sha256(data).hexdigest() != SOURCE_SHA256:
        raise ValueError("Upstream dictionary checksum mismatch")
    rows = data.decode("utf-8-sig").splitlines()
    if int(rows[0]) != len(rows) - 1:
        raise ValueError("Hunspell dictionary entry count does not match its header")
    words = set()
    for row in rows[1:]:
        # Hunspell flags follow a slash; they are metadata, not word characters.
        word = unicodedata.normalize("NFC", row.split("/", 1)[0].strip()).casefold()
        if word.isalpha() and 3 <= len(word) <= 16:
            words.add(word)
    if len(words) != EXPECTED_WORD_COUNT:
        raise ValueError(f"Unexpected filtered dictionary size: {len(words)}")
    return ("\n".join(sorted(words)) + "\n").encode("utf-8")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Verify the existing asset without rewriting it")
    args = parser.parse_args()
    result = build_dictionary()
    if args.check:
        if TARGET.read_bytes() != result:
            parser.exit(1, "Bundled dictionary differs from the reproducible import\n")
    else:
        TARGET.write_bytes(result)
    print(f"{'Verified' if args.check else 'Wrote'} {EXPECTED_WORD_COUNT:,} words; SHA-256 {hashlib.sha256(result).hexdigest()}")


if __name__ == "__main__":
    main()
