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

my %lex;

while( $lexicon =~ m/([^\n ]+)/g ) {
	$lex{$1} = 1;
}


print "############ PHONEME ANALYSIS #############\n";
my @phon = ("p", "b", "t", "d", "k", "g", "s", "S", "r", "l", "a", "i", "u", "^");

my $totalCompetitors = 0;
my $xComp = 0;

foreach my $p( @phon ){
	print "$p : ";
	my $count = 0;
	my $wordCount = 0;
	my @counts = (0, 0, 0, 0, 0, 0, 0, 0, 0);
	foreach my $w ( keys %lex ){
		my $pp = quotemeta $p;
		my $hasPhoneme = 0;
		while( $w =~ m/$pp/g ){
			if( !$hasPhoneme ){
				$wordCount++;
				$hasPhoneme = 1;
			}
			$counts[length($`)] ++;
			#print "$w matches $p at ".length($`)+1."\n"; 
			$count ++;
		}
	}
	foreach( @counts ){
		if( $_ > 0 ){
			$totalCompetitors += $_;
			$xComp ++;
		}
	}
	
	print "total=$count in $wordCount words  positions=[".join(", ", @counts)."]\n";


}
print "Average competition : ".($totalCompetitors/$xComp)."\n";

print "############ LENGTH ANALYSIS #############\n";

my $total = 0;
my @lengths = ();
foreach my $w ( keys %lex ){
	$lengths[length($w)-1] ++;	
	$total += length($w);
}
print join(", ", @lengths);
print " (average=".( $total / keys %lex).")\n";

