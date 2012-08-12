package perltools;
use strict;
use warnings;

my $slurp;
{
    local $/ = undef;
    open my $textfile, '<', $ARGV[0] or die $!;
    $slurp = <$textfile>;
    close $textfile;
    
}

my @fails;

while( $slurp =~ m/([^\n ]+) +([-0-9]+)/g ) {
	if( $2 == -1 ){
		push(@fails, $& );
	}
}


#print "Fails (".@fails.") :\n";
foreach (@fails){
	print "$_\n";
}

