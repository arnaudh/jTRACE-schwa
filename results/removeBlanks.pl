
my $slurp;
{
    local $/ = undef;
    open my $textfile, '<', $ARGV[0] or die $!;
    $reference = <$textfile>;
    close $textfile;
    
}


while( $reference =~ m/([^\n ]+)( +)(  )([-0-9]+)/g ) {
	print "$1$2$4\n";
}