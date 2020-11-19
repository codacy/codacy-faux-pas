Git prefers UTF-8 encoding, and doesn’t handle some other encodings (e.g. UTF-16) very well. It thinks UTF-16 -encoded files are binary, and will thus not display line diffs for them.
