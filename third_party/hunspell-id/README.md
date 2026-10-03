# Vendored Hunspell Indonesian dictionary

Source: https://github.com/shuLhan/hunspell-id/tree/5939c33c8c7b2df9691d9173ca84c8a987902e1f

`id_ID.dic` is unmodified upstream source at that revision, with SHA-256 `42cf27a0ba5966eaf72846419200a0318ef4db42066947bd840b140604b3385c`.
Copyright: hunspell-id Authors. License: LGPL-3.0-only, as identified by the upstream `.reuse/dep5` file retained as `UPSTREAM-LICENSE-MAPPING.txt`.
`COPYING` is the upstream LGPL v3 license. Complete LGPL v3 and GPL v3 texts and modification attribution are also bundled under `app/src/main/assets/dictionary_licenses`.

Regenerate the filtered app asset offline:

```sh
python3 tools/import_hunspell_dictionary.py
python3 tools/import_hunspell_dictionary.py --check
```

The importer normalizes Unicode, lowercases, removes duplicates, and keeps alphabetic words of 3–16 characters. It removes Hunspell metadata and does not generate derived forms from affix rules.
