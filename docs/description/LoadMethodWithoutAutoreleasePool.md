When the +[NSObject load] method is executed, there may not be an autorelease pool in place, so you must set one up yourself. This only applies to iOS 5 (and earlier) and OS X 10.7 (and earlier).
