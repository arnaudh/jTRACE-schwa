
my $slurp;
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
my @words;
while( $reference =~ m/([^\n ]+) +([-0-9]+)/g ) {
	push(@words, $1);
	push(@tREF, $2);
}
my $w = 0;
while( $challenger =~ m/([^\n ]+) +([-0-9]+)/g ) {
	if( $words[$w++] ne $1 ){ die "Words are different : $words[$w] != $1"; }
	push(@tCHALL, $2);
}


### number of words
my $N = @tREF;
if( $N != scalar @tCHALL ){ die "Results are not the same length"; }


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

print "SAME   : ".@iSAME."\n";
print "BETTER : ".@iBETTER."\n";
print_diff( @iBETTER );

print "WORSE  : ".@iWORSE."\n";
print_diff( @iWORSE );

print "YEAH   : ".@iYEAH."\n";
print "NO     : ".@iNO."\n";


sub print_diff { 
	my @diff;
	foreach(@_){
		push(@diff, $tCHALL[$_] - $tREF[$_]);
	}
	print "       mean : ".mean(@diff).", variance : ".variance(@diff)."\n";
	
	my @permutation = reverse( sort { abs($diff[$a]) <=> abs($diff[$b]) } (0..$#diff) );
	my @best = @words[@_[@permutation]];
	my @sortedDiff = @diff[@permutation];
	
	print "          greatest differences :\n";
	for(my $i=0; $i<50; $i++){
		print "          $best[$i] ($sortedDiff[$i])\n";
	}
}
		


sub mean {
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

