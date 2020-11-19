Methods that pass back NSError objects by reference return NO or nil to indicate an error condition. This rule warns if the NSError pointer is checked against nil without checking the return value.
