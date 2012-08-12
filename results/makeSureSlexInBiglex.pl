
my $slex;
my $biglex;
{
    local $/ = undef;
    open my $textfile, '<', '../tools/Lexicons/slex.txt' or die $!;
    $slex = <$textfile>;
    close $textfile;
    
    open my $textfile2, '<', '../tools/Lexicons/biglex901.txt' or die $!;
    $biglex = <$textfile2>;
    close $textfile2;
    
}

my %big;
while( $biglex =~ m/([^\n ]+)/g ){
	$big{ $1 } = 1;
}



while( $slex =~ m/([^\n ]+)/g ) {
	if( !exists( $big{$1} ) ){
		print "$1\n";
	}
}