
my $slurp;
{
    local $/ = undef;
    open my $textfile, '<', 'mayor_phonemes.txt' or die $!;
    $slurp = <$textfile>;
    close $textfile;
}

my @features = ('POW', 'VOC', 'DIF', 'ACU', 'GRD', 'VOI', 'BUR');
my $labels = '';
my $phonDefs = '';
my $durationScalar = '';

# Power Vocalic Diffuse Acute Consonantal Voiced Burst in TRACE(1986)
# example of phoneme :
# @ 7 8 4 1 1 8 - no

while( $slurp =~ m/(.) ([0-9]) ([0-9]) ([0-9]) ([0-9]) ([0-9]) ([0-9]) (-) (yes|no)[ \t]*([^\n]*)/g ) {
	if( $9 eq 'no' ){
		# new phoneme
		$labels .= ", \"$1\"";
		$durationScalar .= "\n       {1, 1, 1, 1, 1, 1, 1}, /*$1*/";
		if ( $10 ne '' ){ #extra information about the phoneme
			#$phonDefs .= "\n/*$10*/";
		}
		$phonDefs .= "\n/*$1*/ { ";
		
		# for each feature
		for( my $f = 2; $f < 9; $f++){
			my $featureValue = $$f;
			# Special case for GRD; GRD = 9-Consonantal
			if( $f == 6 ){
				$featureValue = 9-$featureValue;
			}
		
			# for each dimension
			for( my $d = 8; $d > 0; $d--){
				if( $d eq $featureValue ){
					$phonDefs .= "  1. ,";
				}else{
					$phonDefs .= "  0  ,";
				}
			}
			
			# extra dimension (used only for silence '-')
			$phonDefs .= "  0  ";
			if( $f == 8 ){
				$phonDefs .= "}, ";
			}else{
				$phonDefs .= ",  ";
			}
			
			
			$phonDefs .= "/* $features[$f-2] */ \n";
			
		}
		
    }
}

print "$phonDefs\n";
print "$labels\n";
print "$durationScalar\n";