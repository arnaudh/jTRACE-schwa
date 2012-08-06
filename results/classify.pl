## Functions to classify words (containing schwa, monosyllable, etc)



sub classify {
	$mono = 0; $monoSchwa = 0;
	$bi   = 0; $biSchwa   = 0;
	$tri  = 0; $triSchwa  = 0;
	$quad = 0; $quadSchwa = 0;
	
	foreach( @_ ){		
		# print "$_ contains schwa: ".numberOfSchwas($_).", number of syllables: ".numberOfSyllables($_)."\n";
		$nSyllables = numberOfSyllables( $_ );
		$nSchwas = numberOfSchwas( $_ );
		$schwa = ($nSchwas>0);
		if( $nSyllables == 1 ){
			$mono ++;
			$monoSchwa += $schwa;
		} elsif( $nSyllables == 2 ){
			$bi ++;
			$biSchwa += $schwa;
		} elsif( $nSyllables == 3 ){
			$tri ++;
			$triSchwa += $schwa;
		} elsif( $nSyllables == 4 ){
			$quad ++;
			$quadSchwa += $schwa;
		}else{
			die "unknown number of syllables : $_ ($nSyllables)\n";
		}
	}

	my $total = $mono + $bi + $tri + $quad;
	print "Mono: $mono , Bi  : $bi, Tri : $tri, Quad: $quad\n";
}


sub numberOfSchwas {
	my $nb = 0;
	while( $_[0] =~ /\^/g ){ $nb++; }
	return $nb;
}


## pre-load number of syllables
my %lexicalStress = ();
{
    local $/ = undef;
    ### main lexicon
    open my $textfile, '<', '../tools/Lexicons/biglex901STRESS.txt' or die $!;
    $biglex = <$textfile>;
    close $textfile;
    while( $biglex =~ /([^\s\n]+)\s*\n([W S]+)/g ){
    	$lexicalStress{$1} = $2;
    }
    ### grammatical words
    open my $textfile2, '<', '../tools/Lexicons/grammatical.txt' or die $!;
    $gramm = <$textfile2>;
    close $textfile2;
    while( $gramm =~ /([^\s\n]+)/g ){
    	$lexicalStress{$1} = "W";
    }
}

sub numberOfSyllables {
	my $stress = $lexicalStress{ $_[0] };
	my $nb = 0;
	while( $stress =~ /[WS]/g ){ $nb++; }
	return $nb;
}




1;