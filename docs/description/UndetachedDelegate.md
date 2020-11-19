When an object sets itself as the delegate or data source of one of its members, it must detach that reference in its -[NSObject dealloc] method.
