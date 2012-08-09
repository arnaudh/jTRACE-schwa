package perltools;
use strict;
use warnings;

my $reference;
my $challenger;
{
    local $/ = undef;
    open my $textfile, '<', $ARGV[0] or die $!;
    $reference = <$textfile>;
    close $textfile;
    
    open my $textfile2, '<', $ARGV[1] or die $!;
    $challenger = <$textfile2>;
    close $textfile2;
    
}

### actual rows from the results file
my @ref;
my @chall;

### indices of fails
my %failsREF;
my %failsCHALL;

my $index = 0;
while( $reference =~ m/([^\n ]+) +([-0-9]+) + ([^\n ]+) +([-0-9]+)/g ) {
	push( @ref, $& );
	if( $2 == -1 || $4 == -1 ){
		$failsREF{ $index } = 1;
	}
	
	$challenger =~ m/([^\n ]+) +([-0-9]+) + ([^\n ]+) +([-0-9]+)/g;
	push( @chall, $& );
	if( $2 == -1 || $4 == -1 ){
		$failsCHALL{ $index } = 1;
	}
	
	$index++;
}



foreach(keys %failsREF){
	# if the challenger doesn't fail on that one, it's an improvement!
	if( !exists($failsCHALL{$_} ) ){
		print "Challenger doesn't fail on : $_ (".$chall[$_].")\n";
	}	
}
	
