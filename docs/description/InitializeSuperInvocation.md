Implementations of +[NSObject initialize] should not invoke the superclass implementation: this method is special in that the runtime invokes it separately for every subclass.
