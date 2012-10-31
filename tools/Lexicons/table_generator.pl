package perltools;
use strict;
use warnings;

my $lexicon;
{
    if( exists( $ARGV[0] ) ){
    local $/ = undef;
	    open my $textfile, '<', $ARGV[0] or die $!;
	    $lexicon = <$textfile>;
	    close $textfile;
    }else{
    
    	my $line;
		while (defined($line = <STDIN>)) {
			$lexicon .= "$line\n";
		}
		
	}
    
}

my @lex;

while( $lexicon =~ m/([^\n ]+)/g ) {
	push( @lex, $1 );
}


my $nRows = 10;

for( my $row = 0; $row < $nRows; $row ++){
	my $col = 0;
	while( exists( $lex[$row + $col * $nRows] ) ){
		if($col>0){ print "&";} 
		my $l = 6 - length($lex[$row + $col * $nRows]);
		print " $lex[$row + $col * $nRows] "." "x$l;
		$col++;
	}
	
	print "\\\\\n";
}

