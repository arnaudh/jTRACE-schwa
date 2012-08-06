package perltools;
use strict;
use warnings;
require "classify.pl";

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


### words and recognition points of the reference and the challenger
my @tREF;
my @tCHALL;
my @wordsREF;
my @words;
## start with challenger (can have fewer words)
while( $challenger =~ m/([^\n ]+) +([-0-9]+)/g ) {
	push(@words, $1);
	push(@tCHALL, $2);
}
## take only the words in reference that are in challenger
my $currentWord = 0;
while( $reference =~ m/([^\n ]+) +([-0-9]+)/g ) {
	if( $1 eq $words[$currentWord]){ ## word found in challenger list
		push(@tREF, $2);
		$currentWord++;
		if( $currentWord == @words ){
			last;
		}
	}
}
my $w = 0;




### number of words
my $N = @tREF;
if( $N != scalar @tCHALL ){ die "Results are not the same length; couldn't find $words[$currentWord] in reference\n"; }


### indexes for each of the cases
my @iSAME;
my @iBETTER;
my @iWORSE;
my @iYEAH;
my @iNO;
for(my $i = 0; $i < $N; $i++){
	my $t0 = $tREF[$i];
	my $t1 = $tCHALL[$i];
	if( $t0 == $t1 ){
		push(@iSAME, $i);
	}elsif( $t0 == -1 ){
		push(@iYEAH, $i);
	}elsif( $t1 == -1 ){
		push(@iNO, $i);
	}elsif( $t1 < $t0 ){
		push(@iBETTER, $i);
	}else{
		push(@iWORSE, $i);
	}
}

my $total = @iSAME + @iBETTER + @iWORSE + @iYEAH + @iNO;
print "SAME   : ".@iSAME."  (".sprintf("%.0f", @iSAME/$total*100)."\%)\n";
print "BETTER : ".@iBETTER."  (".sprintf("%.0f", @iBETTER/$total*100)."\%)\n";
#print_diff( @iBETTER );
print "WORSE  : ".@iWORSE."  (".sprintf("%.0f", @iWORSE/$total*100)."\%)\n";
#print_diff( @iWORSE );
print "YEAH   : ".@iYEAH."  (".sprintf("%.0f", @iYEAH/$total*100)."\%)\n";
#print_diff( @iYEAH );
print "NO     : ".@iNO."  (".sprintf("%.0f", @iNO/$total*100)."\%)\n";

print "  BETTER or YES:\n";
classify(@words[(@iBETTER, @iYEAH)]);
print "  WORSE or FAIL:\n";
classify(@words[(@iWORSE, @iNO)]);




### functions

sub print_diff { 
	my @diff;
	foreach(@_){
		push(@diff, $tCHALL[$_] - $tREF[$_]);
	}
	print "       mean : ".mean(@diff).", std dev : ".sqrt(variance(@diff))."\n";
	
	my @permutation = reverse( sort { abs($diff[$a]) <=> abs($diff[$b]) } (0..$#diff) );
	my @best = @words[@_[@permutation]];
	my @sortedDiff = @diff[@permutation];
	
	print "          greatest differences :\n";
	for(my $i=0; $i<20 && $i<@best; $i++){
		print "          $best[$i] ($sortedDiff[$i])\n";
	}
}
		


sub mean {
if( @_ == 0 ){ return 0;}
  my $sum = 0 ;
  foreach (@_) {
    $sum += $_ ;
  }
  return $sum/@_ ;
}

sub variance {
  my $mean = mean (@_) ;
  my $sum_squares = 0 ;
  foreach (@_) {
    $sum_squares += ($_ - $mean) ** 2 ;
  }
  return $sum_squares/ (@_ - 1) ;
}

